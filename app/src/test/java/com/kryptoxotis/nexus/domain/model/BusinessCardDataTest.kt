package com.kryptoxotis.nexus.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BusinessCardDataTest {

    @Test
    fun `toVCard starts with BEGIN VCARD`() {
        val card = BusinessCardData(name = "Alice")
        assertTrue(card.toVCard().startsWith("BEGIN:VCARD"))
    }

    @Test
    fun `toVCard ends with END VCARD`() {
        val card = BusinessCardData(name = "Alice")
        assertTrue(card.toVCard().endsWith("END:VCARD"))
    }

    @Test
    fun `toVCard includes FN when name is present`() {
        val card = BusinessCardData(name = "Alice Smith")
        assertTrue(card.toVCard().contains("FN:Alice Smith"))
    }

    @Test
    fun `toVCard omits FN when name is blank`() {
        val card = BusinessCardData(name = "")
        assertFalse(card.toVCard().contains("FN:"))
    }

    @Test
    fun `toVCard strips carriage returns from field values`() {
        val card = BusinessCardData(name = "Alice\r\nSmith")
        val vcard = card.toVCard()
        assertTrue(vcard.contains("FN:Alice Smith"))
        assertFalse(vcard.contains("\r"))
    }

    @Test
    fun `toVCard prevents vCard injection via newline in name`() {
        val malicious = "Evil\nBEGIN:VCARD\nVERSION:3.0\nFN:Hacker"
        val card = BusinessCardData(name = malicious)
        val vcard = card.toVCard()
        val lines = vcard.lines()
        val beginCount = lines.count { it.trim() == "BEGIN:VCARD" }
        assertEquals("Should only have one BEGIN:VCARD", 1, beginCount)
    }

    @Test
    fun `toVCard escapes semicolons in field values`() {
        val card = BusinessCardData(company = "Smith; Jones LLC")
        val vcard = card.toVCard()
        assertTrue(vcard.contains("ORG:Smith\\; Jones LLC"))
    }

    @Test
    fun `toVCard escapes backslashes in field values`() {
        val card = BusinessCardData(name = "back\\slash")
        val vcard = card.toVCard()
        val fnLine = vcard.lines().first { it.startsWith("FN:") }
        // sanitize doubles each backslash: back\slash → back\\slash
        assertEquals("FN:back\\\\slash", fnLine)
    }

    @Test
    fun `fromJson round-trips all fields`() {
        val original = BusinessCardData(
            name = "Alice", jobTitle = "Dev", company = "Co",
            phone = "123", email = "a@b.c", website = "https://x.com",
            address = "1 Main", linkedin = "li", instagram = "ig",
            twitter = "tw", github = "gh", organizationId = "org-1"
        )
        val restored = BusinessCardData.fromJson(original.toJson())
        assertEquals(original, restored)
    }

    @Test
    fun `fromJson with invalid JSON returns empty BusinessCardData`() {
        val result = BusinessCardData.fromJson("not json")
        assertEquals(BusinessCardData(), result)
    }

    @Test
    fun `subtitle combines jobTitle and company`() {
        val card = BusinessCardData(name = "Alice", jobTitle = "Dev", company = "Acme")
        assertEquals("Dev at Acme", card.subtitle())
    }

    @Test
    fun `subtitle returns only jobTitle when company is blank`() {
        val card = BusinessCardData(name = "Alice", jobTitle = "Dev")
        assertEquals("Dev", card.subtitle())
    }

    @Test
    fun `subtitle returns name when both jobTitle and company are blank`() {
        val card = BusinessCardData(name = "Alice")
        assertEquals("Alice", card.subtitle())
    }

    // ── fromVCard ──

    @Test
    fun `fromVCard extracts basic fields`() {
        val vcard = """
            BEGIN:VCARD
            VERSION:3.0
            FN:Alice Smith
            TITLE:Engineer
            ORG:Acme Corp
            TEL:555-1234
            EMAIL:alice@acme.com
            URL:https://acme.com
            END:VCARD
        """.trimIndent()
        val card = BusinessCardData.fromVCard(vcard)
        assertEquals("Alice Smith", card.name)
        assertEquals("Engineer", card.jobTitle)
        assertEquals("Acme Corp", card.company)
        assertEquals("555-1234", card.phone)
        assertEquals("alice@acme.com", card.email)
        assertEquals("https://acme.com", card.website)
    }

    @Test
    fun `fromVCard extracts social profiles`() {
        val vcard = """
            BEGIN:VCARD
            VERSION:3.0
            FN:Bob
            X-SOCIALPROFILE;type=linkedin:https://linkedin.com/in/bob
            X-SOCIALPROFILE;type=instagram:@bob
            X-SOCIALPROFILE;type=twitter:@bobtweets
            X-SOCIALPROFILE;type=github:bob-dev
            END:VCARD
        """.trimIndent()
        val card = BusinessCardData.fromVCard(vcard)
        assertEquals("https://linkedin.com/in/bob", card.linkedin)
        assertEquals("@bob", card.instagram)
        assertEquals("@bobtweets", card.twitter)
        assertEquals("bob-dev", card.github)
    }

    @Test
    fun `fromVCard extracts organization ID`() {
        val vcard = "BEGIN:VCARD\nVERSION:3.0\nFN:Test\nX-NEXUS-ORG:abc-123\nEND:VCARD"
        val card = BusinessCardData.fromVCard(vcard)
        assertEquals("abc-123", card.organizationId)
    }

    @Test
    fun `fromVCard unescapes vCard characters`() {
        val vcard = "BEGIN:VCARD\nVERSION:3.0\nFN:Smith\\, Jones\nORG:A\\;B\nEND:VCARD"
        val card = BusinessCardData.fromVCard(vcard)
        assertEquals("Smith, Jones", card.name)
        assertEquals("A;B", card.company)
    }

    @Test
    fun `fromVCard unescapes backslash`() {
        val vcard = "BEGIN:VCARD\nVERSION:3.0\nFN:C\\\\Users\nEND:VCARD"
        val card = BusinessCardData.fromVCard(vcard)
        assertEquals("C\\Users", card.name)
    }

    @Test
    fun `fromVCard returns empty fields for minimal vcard`() {
        val vcard = "BEGIN:VCARD\nVERSION:3.0\nEND:VCARD"
        val card = BusinessCardData.fromVCard(vcard)
        assertEquals("", card.name)
        assertEquals("", card.email)
        assertEquals("", card.organizationId)
    }

    @Test
    fun `fromVCard social profiles are case insensitive`() {
        val vcard = "BEGIN:VCARD\nVERSION:3.0\nX-SOCIALPROFILE;TYPE=LINKEDIN:url\nEND:VCARD"
        val card = BusinessCardData.fromVCard(vcard)
        assertEquals("url", card.linkedin)
    }

    @Test
    fun `fromVCard round-trips with toVCard`() {
        val original = BusinessCardData(
            name = "Alice", jobTitle = "Dev", company = "Co",
            phone = "123", email = "a@b.c", website = "https://x.com",
            linkedin = "li", instagram = "ig",
            twitter = "tw", github = "gh", organizationId = "org-1"
        )
        val restored = BusinessCardData.fromVCard(original.toVCard())
        assertEquals(original.name, restored.name)
        assertEquals(original.jobTitle, restored.jobTitle)
        assertEquals(original.company, restored.company)
        assertEquals(original.phone, restored.phone)
        assertEquals(original.email, restored.email)
        assertEquals(original.website, restored.website)
        assertEquals(original.linkedin, restored.linkedin)
        assertEquals(original.instagram, restored.instagram)
        assertEquals(original.twitter, restored.twitter)
        assertEquals(original.github, restored.github)
        assertEquals(original.organizationId, restored.organizationId)
    }

    @Test
    fun `fromVCard ignores unknown lines`() {
        val vcard = "BEGIN:VCARD\nVERSION:3.0\nFN:Alice\nX-UNKNOWN:value\nNOTE:some note\nEND:VCARD"
        val card = BusinessCardData.fromVCard(vcard)
        assertEquals("Alice", card.name)
    }

    // ── CardType ──

    @Test
    fun `CardType fromString is case insensitive`() {
        assertEquals(CardType.LINK, CardType.fromString("link"))
        assertEquals(CardType.LINK, CardType.fromString("LINK"))
    }

    @Test
    fun `CardType fromString returns CUSTOM for unknown`() {
        assertEquals(CardType.CUSTOM, CardType.fromString("nonexistent"))
    }

    @Test
    fun `CardType fromString resolves all enum values`() {
        CardType.entries.forEach { type ->
            assertEquals(type, CardType.fromString(type.name))
            assertEquals(type, CardType.fromString(type.name.lowercase()))
        }
    }

    @Test
    fun `toVCard escapes commas in field values`() {
        val card = BusinessCardData(company = "Smith, Jones LLC")
        val vcard = card.toVCard()
        assertTrue(vcard.contains("ORG:Smith\\, Jones LLC"))
    }

    @Test
    fun `unescapeVCard collapses backslash-n to space`() {
        val result = BusinessCardData.unescapeVCard("Hello\\nWorld")
        assertEquals("Hello World", result)
    }

    @Test
    fun `fromVCard does not parse ADR field into address`() {
        val vcard = "BEGIN:VCARD\nVERSION:3.0\nFN:Test\nADR:;;123 Main St;;;;\nEND:VCARD"
        val card = BusinessCardData.fromVCard(vcard)
        // fromVCard currently does not parse ADR — address remains empty
        assertEquals("", card.address)
    }

    @Test
    fun `default BusinessCardData has all empty fields`() {
        val card = BusinessCardData()
        assertEquals("", card.name)
        assertEquals("", card.email)
        assertEquals("", card.phone)
        assertEquals("", card.organizationId)
        assertEquals("", card.address)
    }
}
