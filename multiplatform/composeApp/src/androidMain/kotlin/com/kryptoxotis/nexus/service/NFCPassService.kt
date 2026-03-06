package com.kryptoxotis.nexus.service

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

class NFCPassService : HostApduService() {

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
            0x00, 0x0F,
            0x20,
            0xFF.toByte(), 0xFF.toByte(),
            0xFF.toByte(), 0xFF.toByte(),
            0x04,
            0x06,
            0xE1.toByte(), 0x04,
            0x04, 0x00,
            0x00,
            0xFF.toByte()
        )

        fun createNdefMessage(content: String, isUri: Boolean = false): ByteArray {
            val record = if (isUri) createUriRecord(content) else createTextRecord(content)
            val ndefFile = ByteArray(2 + record.size)
            ndefFile[0] = ((record.size shr 8) and 0xFF).toByte()
            ndefFile[1] = (record.size and 0xFF).toByte()
            System.arraycopy(record, 0, ndefFile, 2, record.size)
            return ndefFile
        }

        private fun createUriRecord(uri: String): ByteArray {
            val (prefixCode, strippedUri) = when {
                uri.startsWith("https://www.") -> Pair(0x02.toByte(), uri.removePrefix("https://www."))
                uri.startsWith("http://www.") -> Pair(0x01.toByte(), uri.removePrefix("http://www."))
                uri.startsWith("https://") -> Pair(0x04.toByte(), uri.removePrefix("https://"))
                uri.startsWith("http://") -> Pair(0x03.toByte(), uri.removePrefix("http://"))
                uri.startsWith("tel:") -> Pair(0x05.toByte(), uri.removePrefix("tel:"))
                uri.startsWith("mailto:") -> Pair(0x06.toByte(), uri.removePrefix("mailto:"))
                else -> Pair(0x04.toByte(), uri)
            }
            val uriBytes = strippedUri.toByteArray(Charsets.UTF_8)
            val payload = ByteArray(1 + uriBytes.size)
            payload[0] = prefixCode
            System.arraycopy(uriBytes, 0, payload, 1, uriBytes.size)
            return buildNdefRecord(0x55, payload)
        }

        private fun createTextRecord(text: String): ByteArray {
            val language = "en".toByteArray(Charsets.US_ASCII)
            val textBytes = text.toByteArray(Charsets.UTF_8)
            val payload = ByteArray(1 + language.size + textBytes.size)
            payload[0] = language.size.toByte()
            System.arraycopy(language, 0, payload, 1, language.size)
            System.arraycopy(textBytes, 0, payload, 1 + language.size, textBytes.size)
            return buildNdefRecord(0x54, payload)
        }

        private fun buildNdefRecord(typeCode: Int, payload: ByteArray): ByteArray {
            val shortRecord = payload.size <= 255
            val headerSize = if (shortRecord) 3 else 6
            val record = ByteArray(headerSize + 1 + payload.size)
            record[0] = if (shortRecord) 0xD1.toByte() else 0xC1.toByte()
            record[1] = 0x01
            if (shortRecord) {
                record[2] = payload.size.toByte()
                record[3] = typeCode.toByte()
                System.arraycopy(payload, 0, record, 4, payload.size)
            } else {
                record[2] = ((payload.size shr 24) and 0xFF).toByte()
                record[3] = ((payload.size shr 16) and 0xFF).toByte()
                record[4] = ((payload.size shr 8) and 0xFF).toByte()
                record[5] = (payload.size and 0xFF).toByte()
                record[6] = typeCode.toByte()
                System.arraycopy(payload, 0, record, 7, payload.size)
            }
            return record
        }
    }

    override fun onDeactivated(reason: Int) {
        selectedFile = null
        appSelected = false
    }

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        if (commandApdu.size < 4) return SW_ERROR

        val ins = commandApdu[1]
        val p1 = commandApdu[2]

        return when (ins) {
            INS_SELECT -> handleSelect(commandApdu)
            INS_READ_BINARY -> handleReadBinary(commandApdu)
            INS_UPDATE_BINARY -> SW_CONDITIONS_NOT_MET
            else -> SW_NOT_SUPPORTED
        }
    }

    private fun handleSelect(apdu: ByteArray): ByteArray {
        val p1 = apdu[2]
        val p2 = apdu[3]

        if (p1 == 0x04.toByte()) {
            if (apdu.size < 5) return SW_ERROR
            val lc = apdu[4].toInt() and 0xFF
            if (apdu.size < 5 + lc) return SW_ERROR
            val aid = apdu.copyOfRange(5, 5 + lc)

            if (aid.contentEquals(NDEF_AID)) {
                appSelected = true
                selectedFile = null
                refreshNdefMessageSync()
                return SW_OK
            }
            return SW_NOT_FOUND
        }

        if (p1 == 0x00.toByte() && (p2 == 0x0C.toByte() || p2 == 0x00.toByte())) {
            if (!appSelected) return SW_CONDITIONS_NOT_MET
            if (apdu.size < 7) return SW_ERROR
            val lc = apdu[4].toInt() and 0xFF
            if (lc != 2 || apdu.size < 7) return SW_WRONG_LENGTH
            val fileId = apdu.copyOfRange(5, 7)

            return when {
                fileId.contentEquals(CC_FILE_ID) -> { selectedFile = CC_FILE; SW_OK }
                fileId.contentEquals(NDEF_FILE_ID) -> { selectedFile = ndefMessage; SW_OK }
                else -> SW_NOT_FOUND
            }
        }

        return SW_NOT_FOUND
    }

    private fun handleReadBinary(apdu: ByteArray): ByteArray {
        if (!appSelected) return SW_CONDITIONS_NOT_MET
        val file = selectedFile ?: return SW_ERROR

        val offset = ((apdu[2].toInt() and 0xFF) shl 8) or (apdu[3].toInt() and 0xFF)

        val le = if (apdu.size == 7 && apdu[4] == 0x00.toByte()) {
            val extLe = ((apdu[5].toInt() and 0xFF) shl 8) or (apdu[6].toInt() and 0xFF)
            if (extLe == 0) 65536 else extLe
        } else if (apdu.size >= 5) {
            val rawLe = apdu[apdu.size - 1].toInt() and 0xFF
            if (rawLe == 0) 256 else rawLe
        } else {
            file.size - offset
        }

        if (offset > file.size) return SW_WRONG_LENGTH

        val available = file.size - offset
        val bytesToRead = minOf(le, available)
        val data = file.copyOfRange(offset, offset + bytesToRead)
        return data + SW_OK
    }

    private fun refreshNdefMessageSync() {
        try {
            val cached = NdefCache.read(applicationContext)
            if (cached != null) {
                ndefMessage = cached
                return
            }
            ndefMessage = createNdefMessage("NO_ACTIVE_CARD")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh NDEF message", e)
        }
    }
}
