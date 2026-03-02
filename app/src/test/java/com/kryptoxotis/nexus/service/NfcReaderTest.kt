package com.kryptoxotis.nexus.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NfcReaderTest {

    // ── isOk ──

    @Test
    fun `isOk returns true for 9000 status`() {
        val resp = byteArrayOf(0x90.toByte(), 0x00)
        assertTrue(NfcReader.isOk(resp))
    }

    @Test
    fun `isOk returns true for data followed by 9000`() {
        val resp = byteArrayOf(0x01, 0x02, 0x03, 0x90.toByte(), 0x00)
        assertTrue(NfcReader.isOk(resp))
    }

    @Test
    fun `isOk returns false for error status 6A82`() {
        val resp = byteArrayOf(0x6A, 0x82.toByte())
        assertFalse(NfcReader.isOk(resp))
    }

    @Test
    fun `isOk returns false for empty array`() {
        assertFalse(NfcReader.isOk(byteArrayOf()))
    }

    @Test
    fun `isOk returns false for single byte`() {
        assertFalse(NfcReader.isOk(byteArrayOf(0x90.toByte())))
    }

    // ── parseNdefRecord: Text records ──

    @Test
    fun `parseNdefRecord decodes short text record`() {
        val lang = "en".toByteArray()
        val text = "Hello".toByteArray()
        val payload = ByteArray(1 + lang.size + text.size)
        payload[0] = lang.size.toByte()
        lang.copyInto(payload, 1)
        text.copyInto(payload, 1 + lang.size)

        val type = byteArrayOf(0x54) // 'T'
        val header = byteArrayOf(
            (0x01 or 0x10).toByte(), // TNF=1, SR=1
            type.size.toByte(),
            payload.size.toByte()
        )
        val record = header + type + payload

        assertEquals("Hello", NfcReader.parseNdefRecord(record))
    }

    @Test
    fun `parseNdefRecord decodes long text record`() {
        val lang = "en".toByteArray()
        val text = "Hello World".toByteArray()
        val payload = ByteArray(1 + lang.size + text.size)
        payload[0] = lang.size.toByte()
        lang.copyInto(payload, 1)
        text.copyInto(payload, 1 + lang.size)

        val type = byteArrayOf(0x54) // 'T'
        val header = byteArrayOf(
            0x01, // TNF=1, SR=0
            type.size.toByte(),
            0x00, 0x00, 0x00, payload.size.toByte() // 4-byte payload length
        )
        val record = header + type + payload

        assertEquals("Hello World", NfcReader.parseNdefRecord(record))
    }

    @Test
    fun `parseNdefRecord returns null for text record with langLen exceeding payload`() {
        // Status byte claims langLen=10 but payload is only 3 bytes total
        val payload = byteArrayOf(0x0A, 0x41, 0x42) // langLen=10, only 2 bytes of data
        val type = byteArrayOf(0x54) // 'T'
        val header = byteArrayOf(
            (0x01 or 0x10).toByte(),
            type.size.toByte(),
            payload.size.toByte()
        )
        val record = header + type + payload

        assertNull(NfcReader.parseNdefRecord(record))
    }

    // ── parseNdefRecord: URI records ──

    @Test
    fun `parseNdefRecord decodes URI with https prefix`() {
        val body = "example.com".toByteArray()
        val payload = ByteArray(1 + body.size)
        payload[0] = 0x04 // https://
        body.copyInto(payload, 1)

        val type = byteArrayOf(0x55) // 'U'
        val header = byteArrayOf(
            (0x01 or 0x10).toByte(),
            type.size.toByte(),
            payload.size.toByte()
        )
        val record = header + type + payload

        assertEquals("https://example.com", NfcReader.parseNdefRecord(record))
    }

    @Test
    fun `parseNdefRecord decodes URI with http www prefix`() {
        val body = "example.com".toByteArray()
        val payload = ByteArray(1 + body.size)
        payload[0] = 0x01 // http://www.
        body.copyInto(payload, 1)

        val type = byteArrayOf(0x55)
        val header = byteArrayOf(
            (0x01 or 0x10).toByte(),
            type.size.toByte(),
            payload.size.toByte()
        )
        val record = header + type + payload

        assertEquals("http://www.example.com", NfcReader.parseNdefRecord(record))
    }

    @Test
    fun `parseNdefRecord decodes URI with https www prefix`() {
        val body = "example.com".toByteArray()
        val payload = ByteArray(1 + body.size)
        payload[0] = 0x02 // https://www.
        body.copyInto(payload, 1)

        val type = byteArrayOf(0x55)
        val header = byteArrayOf(
            (0x01 or 0x10).toByte(),
            type.size.toByte(),
            payload.size.toByte()
        )
        val record = header + type + payload

        assertEquals("https://www.example.com", NfcReader.parseNdefRecord(record))
    }

    @Test
    fun `parseNdefRecord decodes URI with http prefix`() {
        val body = "example.com".toByteArray()
        val payload = ByteArray(1 + body.size)
        payload[0] = 0x03 // http://
        body.copyInto(payload, 1)

        val type = byteArrayOf(0x55)
        val header = byteArrayOf(
            (0x01 or 0x10).toByte(),
            type.size.toByte(),
            payload.size.toByte()
        )
        val record = header + type + payload

        assertEquals("http://example.com", NfcReader.parseNdefRecord(record))
    }

    @Test
    fun `parseNdefRecord URI with prefix 0 and https body is allowed`() {
        val body = "https://example.com".toByteArray()
        val payload = ByteArray(1 + body.size)
        payload[0] = 0x00
        body.copyInto(payload, 1)

        val type = byteArrayOf(0x55)
        val header = byteArrayOf(
            (0x01 or 0x10).toByte(),
            type.size.toByte(),
            payload.size.toByte()
        )
        val record = header + type + payload

        assertEquals("https://example.com", NfcReader.parseNdefRecord(record))
    }

    @Test
    fun `parseNdefRecord URI with prefix 0 and non-http body is rejected`() {
        val body = "custom://app".toByteArray()
        val payload = ByteArray(1 + body.size)
        payload[0] = 0x00
        body.copyInto(payload, 1)

        val type = byteArrayOf(0x55)
        val header = byteArrayOf(
            (0x01 or 0x10).toByte(),
            type.size.toByte(),
            payload.size.toByte()
        )
        val record = header + type + payload

        assertNull(NfcReader.parseNdefRecord(record))
    }

    @Test
    fun `parseNdefRecord URI with prefix 0 and javascript body is rejected`() {
        val body = "javascript:alert(1)".toByteArray()
        val payload = ByteArray(1 + body.size)
        payload[0] = 0x00
        body.copyInto(payload, 1)

        val type = byteArrayOf(0x55)
        val header = byteArrayOf(
            (0x01 or 0x10).toByte(),
            type.size.toByte(),
            payload.size.toByte()
        )
        val record = header + type + payload

        assertNull(NfcReader.parseNdefRecord(record))
    }

    // ── parseNdefRecord: edge cases ──

    @Test
    fun `parseNdefRecord returns null for too-short data`() {
        assertNull(NfcReader.parseNdefRecord(byteArrayOf(0x01, 0x02)))
    }

    @Test
    fun `parseNdefRecord returns null for empty URI payload`() {
        val type = byteArrayOf(0x55)
        val header = byteArrayOf(
            (0x01 or 0x10).toByte(),
            type.size.toByte(),
            0x00
        )
        val record = header + type

        assertNull(NfcReader.parseNdefRecord(record))
    }

    @Test
    fun `parseNdefRecord returns null for empty Text payload`() {
        val type = byteArrayOf(0x54)
        val header = byteArrayOf(
            (0x01 or 0x10).toByte(),
            type.size.toByte(),
            0x00
        )
        val record = header + type

        assertNull(NfcReader.parseNdefRecord(record))
    }

    @Test
    fun `parseNdefRecord returns null for unsupported TNF`() {
        // TNF=2 (media type), type="x", payload="hello"
        val payload = "hello".toByteArray()
        val type = "x".toByteArray()
        val header = byteArrayOf(
            (0x02 or 0x10).toByte(), // TNF=2, SR=1
            type.size.toByte(),
            payload.size.toByte()
        )
        val record = header + type + payload

        assertNull(NfcReader.parseNdefRecord(record))
    }

    @Test
    fun `parseNdefRecord returns null for unknown well-known type`() {
        // TNF=1, type=0x53 (not 'T' or 'U')
        val payload = "test".toByteArray()
        val type = byteArrayOf(0x53)
        val header = byteArrayOf(
            (0x01 or 0x10).toByte(),
            type.size.toByte(),
            payload.size.toByte()
        )
        val record = header + type + payload

        assertNull(NfcReader.parseNdefRecord(record))
    }

    @Test
    fun `parseNdefRecord returns null when payload length exceeds MAX_NDEF_BYTES`() {
        // Non-short record with payloadLength = 2000 (> MAX_NDEF_BYTES=1024)
        val type = byteArrayOf(0x54) // 'T'
        val header = byteArrayOf(
            0x01, // TNF=1, SR=0
            type.size.toByte(),
            0x00, 0x00, 0x07, 0xD0.toByte() // payloadLength = 2000
        )
        // We don't need actual payload data — the length check should reject it
        val record = header + type

        assertNull(NfcReader.parseNdefRecord(record))
    }

    @Test
    fun `parseNdefRecord returns null when totalNeeded exceeds data size`() {
        // Short record claiming payload of 100 bytes but data is only 5 bytes
        val type = byteArrayOf(0x55)
        val header = byteArrayOf(
            (0x01 or 0x10).toByte(),
            type.size.toByte(),
            100.toByte() // claims 100 byte payload
        )
        val record = header + type + byteArrayOf(0x04) // only 1 byte of payload

        assertNull(NfcReader.parseNdefRecord(record))
    }

    @Test
    fun `MAX_NDEF_BYTES is 1024`() {
        assertEquals(1024, NfcReader.MAX_NDEF_BYTES)
    }

    // ── parseNdefRecord: IL flag (ID Length present) ──

    @Test
    fun `parseNdefRecord handles record with IL flag and ID field`() {
        // Build a URI record with IL=1 and a 3-byte ID
        val id = byteArrayOf(0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte())
        val body = "example.com".toByteArray()
        val payload = ByteArray(1 + body.size)
        payload[0] = 0x04 // https://
        body.copyInto(payload, 1)

        val type = byteArrayOf(0x55) // 'U'
        // flags: TNF=1, SR=1, IL=1 -> 0x01 | 0x10 | 0x08 = 0x19
        val header = byteArrayOf(
            0x19,
            type.size.toByte(),
            payload.size.toByte(),
            id.size.toByte() // ID Length byte
        )
        val record = header + type + id + payload

        assertEquals("https://example.com", NfcReader.parseNdefRecord(record))
    }

    // ── parseNdefRecord: UTF-16 text record ──

    @Test
    fun `parseNdefRecord returns null for UTF-16 text record`() {
        val lang = "en".toByteArray()
        val text = "Hello".toByteArray()
        val payload = ByteArray(1 + lang.size + text.size)
        payload[0] = (0x80 or lang.size).toByte() // UTF-16 flag set
        lang.copyInto(payload, 1)
        text.copyInto(payload, 1 + lang.size)

        val type = byteArrayOf(0x54) // 'T'
        val header = byteArrayOf(
            (0x01 or 0x10).toByte(),
            type.size.toByte(),
            payload.size.toByte()
        )
        val record = header + type + payload

        assertNull(NfcReader.parseNdefRecord(record))
    }

    // ── parseNdefRecord: plain text (non-URI) ──

    @Test
    fun `parseNdefRecord returns plain text that is not a URI`() {
        val lang = "en".toByteArray()
        val text = "John Smith".toByteArray()
        val payload = ByteArray(1 + lang.size + text.size)
        payload[0] = lang.size.toByte()
        lang.copyInto(payload, 1)
        text.copyInto(payload, 1 + lang.size)

        val type = byteArrayOf(0x54) // 'T'
        val header = byteArrayOf(
            (0x01 or 0x10).toByte(),
            type.size.toByte(),
            payload.size.toByte()
        )
        val record = header + type + payload

        assertEquals("John Smith", NfcReader.parseNdefRecord(record))
    }

    // ── parseNdefRecord: security tests ──

    @Test
    fun `parseNdefRecord rejects text record containing javascript URI`() {
        val lang = "en".toByteArray()
        val text = "javascript://alert(1)".toByteArray()
        val payload = ByteArray(1 + lang.size + text.size)
        payload[0] = lang.size.toByte()
        lang.copyInto(payload, 1)
        text.copyInto(payload, 1 + lang.size)

        val type = byteArrayOf(0x54) // 'T'
        val header = byteArrayOf(
            (0x01 or 0x10).toByte(),
            type.size.toByte(),
            payload.size.toByte()
        )
        val record = header + type + payload

        assertNull(NfcReader.parseNdefRecord(record))
    }

    @Test
    fun `parseNdefRecord strips null bytes from URI body`() {
        val body = "exam\u0000ple.com".toByteArray(Charsets.UTF_8)
        val payload = ByteArray(1 + body.size)
        payload[0] = 0x04 // https://
        body.copyInto(payload, 1)

        val type = byteArrayOf(0x55) // 'U'
        val header = byteArrayOf(
            (0x01 or 0x10).toByte(),
            type.size.toByte(),
            payload.size.toByte()
        )
        val record = header + type + payload

        assertEquals("https://example.com", NfcReader.parseNdefRecord(record))
    }

    @Test
    fun `parseNdefRecord strips control chars from plain text`() {
        val lang = "en".toByteArray()
        val text = "Hello\u0001World\u0000".toByteArray(Charsets.UTF_8)
        val payload = ByteArray(1 + lang.size + text.size)
        payload[0] = lang.size.toByte()
        lang.copyInto(payload, 1)
        text.copyInto(payload, 1 + lang.size)

        val type = byteArrayOf(0x54) // 'T'
        val header = byteArrayOf(
            (0x01 or 0x10).toByte(),
            type.size.toByte(),
            payload.size.toByte()
        )
        val record = header + type + payload

        assertEquals("HelloWorld", NfcReader.parseNdefRecord(record))
    }
}
