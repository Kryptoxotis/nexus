package com.kryptoxotis.nexus.service

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import com.kryptoxotis.nexus.data.local.NexusDatabase
import com.kryptoxotis.nexus.data.repository.PersonalCardRepository
import com.kryptoxotis.nexus.domain.model.CardType
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

class NFCPassService : HostApduService() {

    private val repository: PersonalCardRepository by lazy {
        val database = NexusDatabase.getDatabase(applicationContext)
        PersonalCardRepository(database.personalCardDao())
    }

    private var selectedFile: ByteArray? = null
    @Volatile
    private var ndefMessage: ByteArray = createNdefMessage("NO_CARD")
    private var appSelected = false

    companion object {
        private const val TAG = "Nexus:HCE"

        private val NDEF_AID = byteArrayOf(
            0xD2.toByte(), 0x76.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x85.toByte(), 0x01.toByte(), 0x01.toByte()
        )

        private val CC_FILE_ID = byteArrayOf(0xE1.toByte(), 0x03.toByte())
        private val NDEF_FILE_ID = byteArrayOf(0xE1.toByte(), 0x04.toByte())

        private const val INS_SELECT = 0xA4.toByte()
        private const val INS_READ_BINARY = 0xB0.toByte()
        private const val INS_UPDATE_BINARY = 0xD6.toByte()

        private val SW_OK = byteArrayOf(0x90.toByte(), 0x00.toByte())
        private val SW_NOT_FOUND = byteArrayOf(0x6A.toByte(), 0x82.toByte())
        private val SW_NOT_SUPPORTED = byteArrayOf(0x6A.toByte(), 0x81.toByte())
        private val SW_ERROR = byteArrayOf(0x6F.toByte(), 0x00.toByte())
        private val SW_WRONG_LENGTH = byteArrayOf(0x67.toByte(), 0x00.toByte())
        private val SW_CONDITIONS_NOT_MET = byteArrayOf(0x69.toByte(), 0x85.toByte())

        private val CC_FILE = byteArrayOf(
            0x00, 0x0F,                   // CCLEN: 15 bytes
            0x20,                         // Mapping Version 2.0
            0xFF.toByte(), 0xFF.toByte(), // MLe: 65535 (max R-APDU data size)
            0xFF.toByte(), 0xFF.toByte(), // MLc: 65535 (max C-APDU data size)
            0x04,                         // T: NDEF File Control TLV
            0x06,                         // L: 6 bytes
            0xE1.toByte(), 0x04,          // NDEF File ID
            0x04, 0x00,                   // Max NDEF size: 1024
            0x00,                         // Read access: granted
            0xFF.toByte()                 // Write access: denied
        )

        fun createNdefMessage(content: String, isUri: Boolean = false): ByteArray {
            val record = if (isUri) {
                createUriRecord(content)
            } else {
                createTextRecord(content)
            }

            val ndefFile = ByteArray(2 + record.size)
            ndefFile[0] = ((record.size shr 8) and 0xFF).toByte()
            ndefFile[1] = (record.size and 0xFF).toByte()
            System.arraycopy(record, 0, ndefFile, 2, record.size)

            if (ndefFile.size > 1024) {
                Log.w(TAG, "NDEF message too large (${ndefFile.size} bytes), truncating content")
                return createNdefMessage("Content too large", isUri = false)
            }

            Log.d(TAG, "NDEF message created: ${ndefFile.size} bytes, record: ${record.size} bytes")
            return ndefFile
        }

        private fun createNdefRecord(typeByte: Byte, payload: ByteArray): ByteArray {
            val sr = payload.size <= 255
            val headerSize = if (sr) 3 else 6
            val record = ByteArray(headerSize + 1 + payload.size)
            // Flags: MB=1, ME=1, CF=0, SR=conditional, IL=0, TNF=001 (well-known)
            record[0] = if (sr) 0xD1.toByte() else 0xC1.toByte()
            record[1] = 0x01 // type length
            if (sr) {
                record[2] = payload.size.toByte()
            } else {
                record[2] = ((payload.size shr 24) and 0xFF).toByte()
                record[3] = ((payload.size shr 16) and 0xFF).toByte()
                record[4] = ((payload.size shr 8) and 0xFF).toByte()
                record[5] = (payload.size and 0xFF).toByte()
            }
            record[headerSize] = typeByte
            System.arraycopy(payload, 0, record, headerSize + 1, payload.size)
            return record
        }

        private fun createUriRecord(uri: String): ByteArray {
            val (prefixCode, strippedUri) = when {
                uri.startsWith("https://www.") -> Pair(0x02.toByte(), uri.removePrefix("https://www."))
                uri.startsWith("http://www.") -> Pair(0x01.toByte(), uri.removePrefix("http://www."))
                uri.startsWith("https://") -> Pair(0x04.toByte(), uri.removePrefix("https://"))
                uri.startsWith("http://") -> Pair(0x03.toByte(), uri.removePrefix("http://"))
                else -> Pair(0x04.toByte(), uri)
            }

            val uriBytes = strippedUri.toByteArray(Charsets.UTF_8)
            val payload = ByteArray(1 + uriBytes.size)
            payload[0] = prefixCode
            System.arraycopy(uriBytes, 0, payload, 1, uriBytes.size)

            Log.d(TAG, "URI record: prefix=${String.format("%02X", prefixCode)}, uri=$strippedUri")
            return createNdefRecord(0x55, payload) // 'U' = URI
        }

        private fun createTextRecord(text: String): ByteArray {
            val language = "en".toByteArray(Charsets.US_ASCII)
            val textBytes = text.toByteArray(Charsets.UTF_8)

            val payload = ByteArray(1 + language.size + textBytes.size)
            payload[0] = language.size.toByte()
            System.arraycopy(language, 0, payload, 1, language.size)
            System.arraycopy(textBytes, 0, payload, 1 + language.size, textBytes.size)

            return createNdefRecord(0x54, payload) // 'T' = Text
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
        Log.d(TAG, ">>> APDU IN: ${commandApdu.size} bytes")

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

    private fun handleSelect(apdu: ByteArray): ByteArray {
        val p1 = apdu[2]
        val p2 = apdu[3]

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

                // Refresh NDEF message synchronously so reader gets fresh data
                refreshNdefMessageSync()

                Log.d(TAG, "NDEF message ready: ${ndefMessage.size} bytes")
                return SW_OK
            }

            Log.d(TAG, "Rejecting unknown AID: ${aid.toHexString()}")
            return SW_NOT_FOUND
        }

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

        // Handle extended APDU Le format (Samsung/Android sends 3-byte Le: 00 XX XX)
        val le = if (apdu.size == 7 && apdu[4] == 0x00.toByte()) {
            // Extended Le: 3 bytes (00 + 2-byte length)
            val extLe = ((apdu[5].toInt() and 0xFF) shl 8) or (apdu[6].toInt() and 0xFF)
            if (extLe == 0) 65536 else extLe
        } else if (apdu.size >= 5) {
            val rawLe = apdu[apdu.size - 1].toInt() and 0xFF
            if (rawLe == 0) 256 else rawLe
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

    /**
     * Synchronously reads the active card from DB and builds the NDEF message.
     * Called on the binder thread when a reader selects the NDEF AID.
     * Room queries are fast (<10ms) so this won't cause timeouts.
     */
    private fun refreshNdefMessageSync() {
        try {
            val activeCard = runBlocking { withTimeoutOrNull(500L) { repository.getActiveCard() } }
            ndefMessage = if (activeCard != null) {
                Log.d(TAG, "Active card: type=${activeCard.cardType}, title=${activeCard.title}")
                when (activeCard.cardType) {
                    CardType.LINK, CardType.FILE, CardType.SOCIAL_MEDIA -> {
                        val url = activeCard.content ?: activeCard.title
                        createNdefMessage(url, isUri = true)
                    }
                    else -> {
                        createNdefMessage(activeCard.content ?: activeCard.id, isUri = false)
                    }
                }
            } else {
                Log.d(TAG, "No active card found")
                createNdefMessage("NO_ACTIVE_CARD")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh NDEF message", e)
        }
    }
}
