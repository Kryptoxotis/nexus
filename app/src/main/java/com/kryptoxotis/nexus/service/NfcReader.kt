package com.kryptoxotis.nexus.service

import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.util.Log
import com.kryptoxotis.nexus.util.UrlUtils

/**
 * Reads NDEF data from HCE tags using ISO-DEP (Type 4 Tag protocol).
 * All functions are internal for testability.
 */
internal object NfcReader {

    private const val TAG = "Nexus:Scan"
    /** Maximum NDEF payload in bytes. Must match the max-NDEF-size field (bytes 11-12) in [NFCPassService.CC_FILE]. */
    internal const val MAX_NDEF_BYTES = 1024
    private const val ISO_DEP_TIMEOUT_MS = 3000

    // NFC Forum URI Record Type Definition prefix codes (see NFCForum-TS-RTD_URI_1.0, Table 3).
    // Only common HTTP(S) prefixes handled; others fall through to raw.
    private val URI_PREFIXES = mapOf(
        0x01 to "http://www.",
        0x02 to "https://www.",
        0x03 to "http://",
        0x04 to "https://"
    )

    // APDU commands for Type 4 Tag protocol
    private val SELECT_AID_CMD = byteArrayOf(
        0x00, 0xA4.toByte(), 0x04, 0x00, 0x07,
        0xD2.toByte(), 0x76, 0x00, 0x00, 0x85.toByte(), 0x01, 0x01,
        0x00
    )
    private val SELECT_CC_CMD = byteArrayOf(
        0x00, 0xA4.toByte(), 0x00, 0x0C, 0x02,
        0xE1.toByte(), 0x03
    )
    private val READ_CC_CMD = byteArrayOf(0x00, 0xB0.toByte(), 0x00, 0x00, 0x0F)
    private val SELECT_NDEF_CMD = byteArrayOf(
        0x00, 0xA4.toByte(), 0x00, 0x0C, 0x02,
        0xE1.toByte(), 0x04
    )
    private val READ_NDEF_LEN_CMD = byteArrayOf(0x00, 0xB0.toByte(), 0x00, 0x00, 0x02)

    /**
     * Reads NDEF data from an HCE tag by sending APDU commands.
     * Returns the parsed text/URI payload, or null on failure.
     */
    internal fun readNdefFromTag(tag: Tag): String? {
        val isoDep = IsoDep.get(tag) ?: return null
        try {
            isoDep.connect()
            isoDep.timeout = ISO_DEP_TIMEOUT_MS

            // Step 1: SELECT NDEF Application (AID: D2760000850101)
            var resp = isoDep.transceive(SELECT_AID_CMD)
            if (!isOk(resp)) {
                logDebug { "SELECT AID failed: ${resp.toHex()}" }
                return null
            }

            // Step 2: SELECT CC file (E103)
            resp = isoDep.transceive(SELECT_CC_CMD)
            if (!isOk(resp)) {
                logDebug { "SELECT CC failed: ${resp.toHex()}" }
                return null
            }

            // Step 3: READ CC file
            resp = isoDep.transceive(READ_CC_CMD)
            if (!isOk(resp) || resp.size < 17) {
                logDebug { "READ CC failed: ${resp.toHex()}" }
                return null
            }

            // Step 4: SELECT NDEF file (E104)
            resp = isoDep.transceive(SELECT_NDEF_CMD)
            if (!isOk(resp)) {
                logDebug { "SELECT NDEF failed: ${resp.toHex()}" }
                return null
            }

            // Step 5: READ NDEF length (first 2 bytes)
            resp = isoDep.transceive(READ_NDEF_LEN_CMD)
            if (!isOk(resp) || resp.size < 4) {
                logDebug { "READ NDEF length failed: ${resp.toHex()}" }
                return null
            }
            val ndefLen = ((resp[0].toInt() and 0xFF) shl 8) or (resp[1].toInt() and 0xFF)
            if (ndefLen == 0) {
                Log.d(TAG, "Tag contains empty NDEF message")
                return null
            }
            if (ndefLen > MAX_NDEF_BYTES) {
                Log.d(TAG, "NDEF length $ndefLen exceeds MAX_NDEF_BYTES $MAX_NDEF_BYTES")
                return null
            }

            // Step 6: READ NDEF data (use extended APDU for > 255 bytes)
            // Le = ndefLen: request exactly the payload bytes.
            // IsoDep.transceive() appends SW1/SW2 automatically; resp.size == ndefLen + 2 on success.
            val readNdef = if (ndefLen > 255) {
                byteArrayOf(
                    0x00, 0xB0.toByte(), 0x00, 0x02,
                    0x00, ((ndefLen shr 8) and 0xFF).toByte(), (ndefLen and 0xFF).toByte()
                )
            } else {
                byteArrayOf(
                    0x00, 0xB0.toByte(), 0x00, 0x02,
                    ndefLen.toByte()
                )
            }
            resp = isoDep.transceive(readNdef)
            if (!isOk(resp) || resp.size < ndefLen + 2) {
                logDebug { "READ NDEF data failed: ${resp.toHex()}" }
                return null
            }

            // Parse the NDEF record — clamp to declared ndefLen, never read SW bytes
            val safeNdefLen = minOf(ndefLen, resp.size - 2)
            if (safeNdefLen <= 0) {
                Log.d(TAG, "No usable NDEF payload after SW strip")
                return null
            }
            val ndefBytes = resp.copyOfRange(0, safeNdefLen)
            return parseNdefRecord(ndefBytes)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to read tag", e)
            return null
        } finally {
            try { isoDep.close() } catch (_: Exception) {}
        }
    }

    /**
     * Checks if an APDU response ends with SW 90 00 (success).
     */
    internal fun isOk(resp: ByteArray): Boolean {
        return resp.size >= 2 &&
            resp[resp.size - 2] == 0x90.toByte() &&
            resp[resp.size - 1] == 0x00.toByte()
    }

    /**
     * Validates a URI is safe to return (not a blocked scheme).
     * For prefix code 0x00 (no prefix), only http/https URIs are allowed.
     */
    private fun validateUri(uri: String, prefixCode: Int = -1): String? {
        if (UrlUtils.hasBlockedScheme(uri)) return null
        if (prefixCode == 0) {
            val lower = uri.lowercase()
            if (!lower.startsWith("http://") && !lower.startsWith("https://")) return null
        }
        return uri
    }

    /**
     * Parses an NDEF record from raw bytes.
     * Supports well-known URI ('U') and Text ('T') records.
     * Handles the IL (ID Length present) flag for records with an ID field.
     */
    internal fun parseNdefRecord(data: ByteArray): String? {
        if (data.size < 3) return null
        try {
            // Parse NDEF record header
            val flags = data[0].toInt()
            val tnf = flags and 0x07                           // Type Name Format (bits 0-2)
            val il = (flags and 0x08) != 0                     // ID Length present (bit 3)
            val sr = (flags and 0x10) != 0                     // Short Record flag (bit 4)
            val typeLength = data[1].toInt() and 0xFF

            val baseHeaderSize = if (sr) 3 else 6
            if (data.size < baseHeaderSize) return null

            val payloadLength = if (sr) {
                (data[2].toInt() and 0xFF)
            } else {
                ((data[2].toInt() and 0xFF) shl 24) or
                    ((data[3].toInt() and 0xFF) shl 16) or
                    ((data[4].toInt() and 0xFF) shl 8) or
                    (data[5].toInt() and 0xFF)
            }
            if (payloadLength < 0 || payloadLength > MAX_NDEF_BYTES) return null

            // Read ID length if IL flag is set
            val idLen = if (il) {
                if (data.size <= baseHeaderSize) return null
                data[baseHeaderSize].toInt() and 0xFF
            } else 0
            val headerSize = baseHeaderSize + (if (il) 1 else 0)

            val totalNeeded = headerSize + typeLength + idLen + payloadLength
            if (totalNeeded > data.size) return null

            val type = data.copyOfRange(headerSize, headerSize + typeLength)
            // Skip ID bytes (headerSize + typeLength .. + idLen) to get to payload
            val payloadStart = headerSize + typeLength + idLen
            val payload = data.copyOfRange(payloadStart, payloadStart + payloadLength)

            // TNF 0x01 = NFC Forum well-known type
            if (tnf == 1 && typeLength == 1) {
                when (type[0].toInt()) {
                    0x55 -> { // URI record (type 'U')
                        if (payload.isEmpty()) return null
                        val prefixCode = payload[0].toInt() and 0xFF
                        val uriBody = String(payload, 1, payload.size - 1, Charsets.UTF_8)
                            .replace(Regex("[\\x00-\\x1f\\x7f]"), "")
                            .trim()
                        val prefix = URI_PREFIXES[prefixCode] ?: ""
                        return validateUri(prefix + uriBody, prefixCode)
                    }
                    0x54 -> { // Text record (type 'T')
                        if (payload.isEmpty()) return null
                        val statusByte = payload[0].toInt()
                        val isUtf16 = (statusByte and 0x80) != 0
                        if (isUtf16) {
                            Log.d(TAG, "UTF-16 Text records not supported")
                            return null
                        }
                        val langLen = statusByte and 0x3F
                        if (payload.size < 1 + langLen) return null
                        val text = String(payload, 1 + langLen, payload.size - 1 - langLen, Charsets.UTF_8)
                        // Only validate as URI if it looks like one; plain text is sanitized
                        return if (text.contains("://")) {
                            validateUri(text)
                        } else {
                            val sanitized = text.replace(Regex("[\\x00-\\x1f\\x7f]"), "").trim()
                            if (sanitized.isBlank() || sanitized.length > MAX_NDEF_BYTES) null else sanitized
                        }
                    }
                }
            }
            // Unsupported NDEF record type — return null instead of raw bytes
            Log.d(TAG, "Unsupported NDEF TNF=$tnf, typeLen=$typeLength")
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse NDEF record", e)
            return null
        }
    }

    /** Log helper that avoids allocations when debug logging is disabled. */
    private inline fun logDebug(msg: () -> String) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, msg())
    }

    /**
     * Converts a byte array to a hex string (e.g. "9000").
     */
    private fun ByteArray.toHex(): String {
        val sb = StringBuilder(size * 2)
        for (b in this) {
            val v = b.toInt() and 0xFF
            sb.append("0123456789ABCDEF"[v shr 4])
            sb.append("0123456789ABCDEF"[v and 0x0F])
        }
        return sb.toString()
    }
}
