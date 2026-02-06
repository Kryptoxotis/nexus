package com.nfcpass.service

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import com.nfcpass.data.local.PassDatabase
import com.nfcpass.data.repository.PassRepository
import kotlinx.coroutines.runBlocking

/**
 * NFC Host Card Emulation (HCE) Service - iOS Compatible
 *
 * Emulates an NFC Type 4 Tag (NFC Forum spec) containing an NDEF record.
 * Designed to work with iOS background tag reading (iPhone XS+, iOS 13+).
 *
 * iOS NFC flow:
 * 1. iOS sends SELECT for payment AIDs (PPSE) - we REJECT these
 * 2. iOS sends SELECT for NDEF AID (D2760000850101) - we ACCEPT
 * 3. iOS reads Capability Container to learn about our NDEF file
 * 4. iOS reads NDEF file containing the URI or text record
 * 5. iOS shows notification banner with the link/text
 */
class NFCPassService : HostApduService() {

    private val repository: PassRepository by lazy {
        val database = PassDatabase.getDatabase(applicationContext)
        PassRepository(database.passDao())
    }

    private var selectedFile: ByteArray? = null
    private var ndefMessage: ByteArray = createNdefMessage("NO_PASS")
    private var appSelected = false

    companion object {
        private const val TAG = "NFCPass:HCE"

        // Standard NDEF Application AID (NFC Forum Type 4 Tag)
        private val NDEF_AID = byteArrayOf(
            0xD2.toByte(), 0x76.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x85.toByte(), 0x01.toByte(), 0x01.toByte()
        )

        // File IDs per NFC Forum Type 4 Tag spec
        private val CC_FILE_ID = byteArrayOf(0xE1.toByte(), 0x03.toByte())
        private val NDEF_FILE_ID = byteArrayOf(0xE1.toByte(), 0x04.toByte())

        // APDU Instructions
        private const val INS_SELECT = 0xA4.toByte()
        private const val INS_READ_BINARY = 0xB0.toByte()
        private const val INS_UPDATE_BINARY = 0xD6.toByte()

        // Status words
        private val SW_OK = byteArrayOf(0x90.toByte(), 0x00.toByte())
        private val SW_NOT_FOUND = byteArrayOf(0x6A.toByte(), 0x82.toByte())
        private val SW_NOT_SUPPORTED = byteArrayOf(0x6A.toByte(), 0x81.toByte())
        private val SW_ERROR = byteArrayOf(0x6F.toByte(), 0x00.toByte())
        private val SW_WRONG_LENGTH = byteArrayOf(0x67.toByte(), 0x00.toByte())
        private val SW_CONDITIONS_NOT_MET = byteArrayOf(0x69.toByte(), 0x85.toByte())

        /**
         * Capability Container (CC) - tells iOS about our NDEF file.
         *
         * Using NFC Forum Type 4 Tag v2.0 spec:
         * - CCLEN: 15 bytes (total CC size)
         * - Mapping Version: 2.0
         * - MLe: 255 (max bytes we can send in one READ BINARY response)
         * - MLc: 255 (max bytes in command data)
         * - NDEF File Control TLV:
         *   - File ID: E104
         *   - Max file size: 512 bytes
         *   - Read access: 00 (open, no auth needed)
         *   - Write access: FF (no write access)
         */
        private val CC_FILE = byteArrayOf(
            0x00, 0x0F,             // CCLEN = 15 bytes
            0x20,                   // Mapping version 2.0
            0x00, 0xFF.toByte(),    // MLe = 255
            0x00, 0xFF.toByte(),    // MLc = 255
            0x04,                   // NDEF File Control TLV type
            0x06,                   // TLV length = 6
            0xE1.toByte(), 0x04,    // NDEF File ID
            0x02, 0x00,             // Max NDEF file size = 512
            0x00,                   // Read access: open
            0xFF.toByte()           // Write access: denied
        )

        /**
         * Creates an NDEF message.
         * Link set → URI record (auto-opens browser on reader phone)
         * No link → Text record (shows pass ID as notification)
         */
        fun createNdefMessage(passId: String, link: String? = null): ByteArray {
            val record = if (!link.isNullOrBlank()) {
                createUriRecord(link)
            } else {
                createTextRecord(passId)
            }

            // NDEF file = 2-byte length prefix + NDEF message
            val ndefFile = ByteArray(2 + record.size)
            ndefFile[0] = ((record.size shr 8) and 0xFF).toByte()
            ndefFile[1] = (record.size and 0xFF).toByte()
            System.arraycopy(record, 0, ndefFile, 2, record.size)

            Log.d(TAG, "NDEF message created: ${ndefFile.size} bytes, record: ${record.size} bytes")
            return ndefFile
        }

        /**
         * Creates an NDEF URI record.
         * iOS will show a notification like "Open in Safari" when it reads this.
         */
        private fun createUriRecord(uri: String): ByteArray {
            val (prefixCode, strippedUri) = when {
                uri.startsWith("https://www.") -> Pair(0x02.toByte(), uri.removePrefix("https://www."))
                uri.startsWith("http://www.") -> Pair(0x01.toByte(), uri.removePrefix("http://www."))
                uri.startsWith("https://") -> Pair(0x04.toByte(), uri.removePrefix("https://"))
                uri.startsWith("http://") -> Pair(0x03.toByte(), uri.removePrefix("http://"))
                else -> Pair(0x04.toByte(), uri) // Default to https:// prefix
            }

            val uriBytes = strippedUri.toByteArray(Charsets.UTF_8)
            val payload = ByteArray(1 + uriBytes.size)
            payload[0] = prefixCode
            System.arraycopy(uriBytes, 0, payload, 1, uriBytes.size)

            // NDEF record header: MB=1, ME=1, CF=0, SR=1, IL=0, TNF=01 (Well-Known)
            val record = ByteArray(3 + 1 + payload.size)
            record[0] = 0xD1.toByte() // Flags: MB|ME|SR, TNF=01
            record[1] = 0x01          // Type length: 1
            record[2] = payload.size.toByte() // Payload length
            record[3] = 0x55          // Type: "U" (URI)
            System.arraycopy(payload, 0, record, 4, payload.size)

            Log.d(TAG, "URI record: prefix=${String.format("%02X", prefixCode)}, uri=$strippedUri")
            return record
        }

        /**
         * Creates an NDEF Text record.
         * iOS will show the text in a notification banner.
         */
        private fun createTextRecord(text: String): ByteArray {
            val language = "en".toByteArray(Charsets.US_ASCII)
            val textBytes = text.toByteArray(Charsets.UTF_8)

            val payload = ByteArray(1 + language.size + textBytes.size)
            payload[0] = language.size.toByte() // Status: UTF-8, lang code length
            System.arraycopy(language, 0, payload, 1, language.size)
            System.arraycopy(textBytes, 0, payload, 1 + language.size, textBytes.size)

            val record = ByteArray(3 + 1 + payload.size)
            record[0] = 0xD1.toByte()
            record[1] = 0x01
            record[2] = payload.size.toByte()
            record[3] = 0x54 // Type: "T" (Text)
            System.arraycopy(payload, 0, record, 4, payload.size)

            return record
        }

        private fun ByteArray.toHexString(): String = joinToString("") { "%02X".format(it) }
    }

    override fun onDeactivated(reason: Int) {
        val reasonStr = when (reason) {
            DEACTIVATION_LINK_LOSS -> "LINK_LOSS"
            DEACTIVATION_DESELECTED -> "DESELECTED"
            else -> "UNKNOWN($reason)"
        }
        Log.d(TAG, "Deactivated: $reasonStr")
        selectedFile = null
        appSelected = false
    }

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        Log.d(TAG, ">>> APDU IN: ${commandApdu.toHexString()} (${commandApdu.size} bytes)")

        if (commandApdu.size < 4) {
            Log.w(TAG, "APDU too short")
            return SW_ERROR.also { Log.d(TAG, "<<< APDU OUT: ${it.toHexString()}") }
        }

        val cla = commandApdu[0]
        val ins = commandApdu[1]
        val p1 = commandApdu[2]
        val p2 = commandApdu[3]

        Log.d(TAG, "CLA=${String.format("%02X", cla)} INS=${String.format("%02X", ins)} P1=${String.format("%02X", p1)} P2=${String.format("%02X", p2)}")

        val response = when (ins) {
            INS_SELECT -> handleSelect(commandApdu)
            INS_READ_BINARY -> handleReadBinary(commandApdu)
            INS_UPDATE_BINARY -> {
                Log.d(TAG, "UPDATE BINARY rejected (read-only tag)")
                SW_CONDITIONS_NOT_MET
            }
            else -> {
                Log.w(TAG, "Unsupported instruction: ${String.format("%02X", ins)}")
                SW_NOT_SUPPORTED
            }
        }

        Log.d(TAG, "<<< APDU OUT: ${response.toHexString()} (${response.size} bytes)")
        return response
    }

    /**
     * Handles SELECT commands from the reader (iOS/Android).
     *
     * iOS typically sends:
     * 1. SELECT AID for PPSE (payment) - we reject with 6A82
     * 2. SELECT AID for NDEF - we accept with 9000
     * 3. SELECT File for CC (E103) - we accept
     * 4. SELECT File for NDEF (E104) - we accept
     */
    private fun handleSelect(apdu: ByteArray): ByteArray {
        val p1 = apdu[2]
        val p2 = apdu[3]

        // SELECT by AID (P1=04, P2=00)
        if (p1 == 0x04.toByte()) {
            if (apdu.size < 5) return SW_ERROR
            val lc = apdu[4].toInt() and 0xFF
            if (apdu.size < 5 + lc) return SW_ERROR
            val aid = apdu.copyOfRange(5, 5 + lc)

            Log.d(TAG, "SELECT AID: ${aid.toHexString()}")

            if (aid.contentEquals(NDEF_AID)) {
                Log.d(TAG, "*** NDEF Application SELECTED ***")
                appSelected = true
                selectedFile = null

                // Load current active pass
                val activePass = runBlocking { repository.getActivePass() }
                ndefMessage = if (activePass != null) {
                    Log.d(TAG, "Active pass: id=${activePass.passId}, link=${activePass.link}")
                    createNdefMessage(activePass.passId, activePass.link)
                } else {
                    Log.w(TAG, "No active pass set")
                    createNdefMessage("NO_ACTIVE_PASS")
                }

                Log.d(TAG, "NDEF message ready: ${ndefMessage.size} bytes")
                return SW_OK
            }

            // Reject any other AID (like PPSE payment AID)
            Log.d(TAG, "Rejecting unknown AID: ${aid.toHexString()}")
            return SW_NOT_FOUND
        }

        // SELECT by File ID (P1=00, P2=0C or P2=00)
        if (p1 == 0x00.toByte() && (p2 == 0x0C.toByte() || p2 == 0x00.toByte())) {
            if (!appSelected) {
                Log.w(TAG, "File SELECT before app SELECT")
                return SW_CONDITIONS_NOT_MET
            }

            if (apdu.size < 7) return SW_ERROR
            val lc = apdu[4].toInt() and 0xFF
            if (lc != 2 || apdu.size < 7) return SW_WRONG_LENGTH
            val fileId = apdu.copyOfRange(5, 7)

            Log.d(TAG, "SELECT File ID: ${fileId.toHexString()}")

            when {
                fileId.contentEquals(CC_FILE_ID) -> {
                    selectedFile = CC_FILE
                    Log.d(TAG, "CC file selected (${CC_FILE.size} bytes)")
                    return SW_OK
                }
                fileId.contentEquals(NDEF_FILE_ID) -> {
                    selectedFile = ndefMessage
                    Log.d(TAG, "NDEF file selected (${ndefMessage.size} bytes)")
                    return SW_OK
                }
                else -> {
                    Log.w(TAG, "Unknown file ID: ${fileId.toHexString()}")
                    return SW_NOT_FOUND
                }
            }
        }

        Log.w(TAG, "Unknown SELECT: P1=${String.format("%02X", p1)} P2=${String.format("%02X", p2)}")
        return SW_NOT_FOUND
    }

    /**
     * Handles READ BINARY commands.
     * iOS reads data from the currently selected file.
     * Offset is in P1:P2, length to read is in Le (last byte).
     */
    private fun handleReadBinary(apdu: ByteArray): ByteArray {
        if (!appSelected) {
            Log.w(TAG, "READ BINARY before app SELECT")
            return SW_CONDITIONS_NOT_MET
        }

        val file = selectedFile
        if (file == null) {
            Log.w(TAG, "READ BINARY with no file selected")
            return SW_ERROR
        }

        val offset = ((apdu[2].toInt() and 0xFF) shl 8) or (apdu[3].toInt() and 0xFF)

        // Le is the last byte (expected response length)
        val le = if (apdu.size >= 5) {
            val rawLe = apdu[apdu.size - 1].toInt() and 0xFF
            if (rawLe == 0) 256 else rawLe // Le=0 means 256 in ISO 7816
        } else {
            file.size - offset
        }

        Log.d(TAG, "READ BINARY: offset=$offset, le=$le, fileSize=${file.size}")

        if (offset > file.size) {
            Log.w(TAG, "Offset ($offset) beyond file size (${file.size})")
            return SW_WRONG_LENGTH
        }

        val available = file.size - offset
        val bytesToRead = minOf(le, available)
        val data = file.copyOfRange(offset, offset + bytesToRead)

        Log.d(TAG, "Returning $bytesToRead bytes from offset $offset")
        return data + SW_OK
    }
}
