package com.kryptoxotis.nexus.presentation.cards

import android.app.Activity
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kryptoxotis.nexus.domain.model.CardType
import com.kryptoxotis.nexus.presentation.business.BusinessViewModel

/**
 * Scan/Receive screen — puts this phone into NFC reader mode so it can
 * reliably read HCE tags from another Android phone running Nexus.
 *
 * This solves the Android-to-Android NFC problem where both phones try to
 * read each other. Reader mode forces this phone to be the reader.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanCardScreen(
    receivedCardViewModel: ReceivedCardViewModel,
    personalCardViewModel: PersonalCardViewModel? = null,
    businessViewModel: BusinessViewModel? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var scanResult by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(true) }

    // Enable reader mode — this forces this phone to be the reader,
    // which is required for reliably reading HCE from another Android phone.
    DisposableEffect(Unit) {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        if (activity != null && nfcAdapter != null) {
            nfcAdapter.enableReaderMode(
                activity,
                { tag ->
                    // Tag discovered — try to read NDEF from it
                    val result = readNdefFromTag(tag)
                    if (result != null) {
                        scanResult = result
                        isScanning = false
                    }
                },
                NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK or
                    NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                null
            )
        }
        onDispose {
            if (activity != null) {
                try {
                    NfcAdapter.getDefaultAdapter(context)?.disableReaderMode(activity)
                } catch (_: Exception) {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Card") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isScanning) {
                Icon(
                    Icons.Default.Nfc,
                    contentDescription = "NFC scanning",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Hold near a Nexus card",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Place this phone against another phone that has an active Nexus card",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator()
            } else {
                val result = scanResult
                val isVCard = result != null && result.contains("BEGIN:VCARD")
                var contactSaved by remember { mutableStateOf(false) }

                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = if (isVCard) "Business card received!" else "Card received!",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                if (isVCard && result != null) {
                    // Parse and display vCard fields
                    val parsed = parseVCard(result)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (parsed.name.isNotBlank()) {
                        Text(
                            text = parsed.name,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                    val subtitle = listOfNotNull(
                        parsed.jobTitle.ifBlank { null },
                        parsed.company.ifBlank { null }
                    ).joinToString(" at ")
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    if (!contactSaved) {
                        Button(onClick = {
                            receivedCardViewModel.saveContact(
                                name = parsed.name,
                                jobTitle = parsed.jobTitle,
                                company = parsed.company,
                                phone = parsed.phone,
                                email = parsed.email,
                                website = parsed.website,
                                linkedin = parsed.linkedin,
                                instagram = parsed.instagram,
                                twitter = parsed.twitter,
                                github = parsed.github
                            )
                            contactSaved = true
                        }) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Contact")
                        }
                    } else {
                        Text(
                            text = "Contact saved!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Join organization via NFC
                    if (businessViewModel != null && parsed.organizationId.isNotBlank()) {
                        var orgJoined by remember { mutableStateOf(false) }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (!orgJoined) {
                            Button(onClick = {
                                businessViewModel.enrollInOrganization(
                                    parsed.organizationId,
                                    parsed.company.ifBlank { null }
                                )
                                orgJoined = true
                            }) {
                                Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Join ${parsed.company.ifBlank { "Organization" }}")
                            }
                        } else {
                            Text(
                                text = "Enrollment requested!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Save to wallet as a card
                    if (personalCardViewModel != null) {
                        var walletSaved by remember { mutableStateOf(false) }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (!walletSaved) {
                            OutlinedButton(onClick = {
                                val title = parsed.name.ifBlank { "Scanned Contact" }
                                personalCardViewModel.addCard(
                                    cardType = CardType.CONTACT,
                                    title = title,
                                    content = result
                                )
                                walletSaved = true
                            }) {
                                Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save to Wallet")
                            }
                        } else {
                            Text(
                                text = "Saved to wallet!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else if (result != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    val parsedUri = android.net.Uri.parse(result)
                    val isUrl = parsedUri.scheme == "http" || parsedUri.scheme == "https"
                    if (isUrl) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, parsedUri)
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Link")
                        }
                    }

                    // Save to wallet
                    if (personalCardViewModel != null) {
                        var walletSaved by remember { mutableStateOf(false) }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (!walletSaved) {
                            OutlinedButton(onClick = {
                                val cardType = if (isUrl) CardType.LINK else CardType.CUSTOM
                                val title = if (isUrl) {
                                    parsedUri.host?.removePrefix("www.") ?: "Scanned Link"
                                } else {
                                    result.take(50)
                                }
                                personalCardViewModel.addCard(
                                    cardType = cardType,
                                    title = title,
                                    content = result
                                )
                                walletSaved = true
                            }) {
                                Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save to Wallet")
                            }
                        } else {
                            Text(
                                text = "Saved to wallet!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = {
                    scanResult = null
                    isScanning = true
                    contactSaved = false
                }) {
                    Text("Scan Again")
                }
            }
        }
    }
}

/**
 * Reads NDEF data from an HCE tag using ISO-DEP (Type 4 Tag protocol).
 * This manually sends APDU commands to read the NDEF message.
 */
private fun readNdefFromTag(tag: Tag): String? {
    val isoDep = IsoDep.get(tag) ?: return null
    try {
        isoDep.connect()
        isoDep.timeout = 5000

        // Step 1: SELECT NDEF Application (AID: D2760000850101)
        val selectAid = byteArrayOf(
            0x00, 0xA4.toByte(), 0x04, 0x00, 0x07,
            0xD2.toByte(), 0x76, 0x00, 0x00, 0x85.toByte(), 0x01, 0x01,
            0x00
        )
        var resp = isoDep.transceive(selectAid)
        if (!isOk(resp)) {
            Log.d("Nexus:Scan", "SELECT AID failed: ${resp.toHex()}")
            return null
        }

        // Step 2: SELECT CC file (E103)
        val selectCc = byteArrayOf(
            0x00, 0xA4.toByte(), 0x00, 0x0C, 0x02,
            0xE1.toByte(), 0x03
        )
        resp = isoDep.transceive(selectCc)
        if (!isOk(resp)) {
            Log.d("Nexus:Scan", "SELECT CC failed: ${resp.toHex()}")
            return null
        }

        // Step 3: READ CC file
        val readCc = byteArrayOf(0x00, 0xB0.toByte(), 0x00, 0x00, 0x0F)
        resp = isoDep.transceive(readCc)
        if (!isOk(resp) || resp.size < 17) {
            Log.d("Nexus:Scan", "READ CC failed: ${resp.toHex()}")
            return null
        }

        // Step 4: SELECT NDEF file (E104)
        val selectNdef = byteArrayOf(
            0x00, 0xA4.toByte(), 0x00, 0x0C, 0x02,
            0xE1.toByte(), 0x04
        )
        resp = isoDep.transceive(selectNdef)
        if (!isOk(resp)) {
            Log.d("Nexus:Scan", "SELECT NDEF failed: ${resp.toHex()}")
            return null
        }

        // Step 5: READ NDEF length (first 2 bytes)
        val readLen = byteArrayOf(0x00, 0xB0.toByte(), 0x00, 0x00, 0x02)
        resp = isoDep.transceive(readLen)
        if (!isOk(resp) || resp.size < 4) {
            Log.d("Nexus:Scan", "READ NDEF length failed: ${resp.toHex()}")
            return null
        }
        val ndefLen = ((resp[0].toInt() and 0xFF) shl 8) or (resp[1].toInt() and 0xFF)
        if (ndefLen == 0 || ndefLen > 1024) {
            Log.d("Nexus:Scan", "Invalid NDEF length: $ndefLen")
            return null
        }

        // Step 6: READ NDEF data (use extended APDU for > 255 bytes)
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
            Log.d("Nexus:Scan", "READ NDEF data failed: ${resp.toHex()}")
            return null
        }

        // Parse the NDEF record
        val ndefBytes = resp.copyOfRange(0, resp.size - 2)
        return parseNdefRecord(ndefBytes)

    } catch (e: Exception) {
        Log.e("Nexus:Scan", "Failed to read tag", e)
        return null
    } finally {
        try { isoDep.close() } catch (_: Exception) {}
    }
}

private fun isOk(resp: ByteArray): Boolean {
    return resp.size >= 2 &&
        resp[resp.size - 2] == 0x90.toByte() &&
        resp[resp.size - 1] == 0x00.toByte()
}

private fun parseNdefRecord(data: ByteArray): String? {
    if (data.isEmpty()) return null
    try {
        // Parse NDEF record header
        val tnf = data[0].toInt() and 0x07
        val sr = (data[0].toInt() and 0x10) != 0 // short record
        val typeLength = data[1].toInt() and 0xFF
        val payloadLength = if (sr) {
            (data[2].toInt() and 0xFF)
        } else {
            ((data[2].toInt() and 0xFF) shl 24) or
                ((data[3].toInt() and 0xFF) shl 16) or
                ((data[4].toInt() and 0xFF) shl 8) or
                (data[5].toInt() and 0xFF)
        }
        val headerSize = if (sr) 3 else 6
        val type = data.copyOfRange(headerSize, headerSize + typeLength)
        val payload = data.copyOfRange(headerSize + typeLength, headerSize + typeLength + payloadLength)

        // TNF 0x01 = NFC Forum well-known type
        if (tnf == 1 && typeLength == 1) {
            when (type[0].toInt()) {
                0x55 -> { // URI record (type 'U')
                    val prefixCode = payload[0].toInt() and 0xFF
                    val uriBody = String(payload, 1, payload.size - 1, Charsets.UTF_8)
                    val prefix = when (prefixCode) {
                        0x01 -> "http://www."
                        0x02 -> "https://www."
                        0x03 -> "http://"
                        0x04 -> "https://"
                        else -> ""
                    }
                    return prefix + uriBody
                }
                0x54 -> { // Text record (type 'T')
                    val langLen = payload[0].toInt() and 0x3F
                    return String(payload, 1 + langLen, payload.size - 1 - langLen, Charsets.UTF_8)
                }
            }
        }
        // Fallback: return raw text
        return String(payload, Charsets.UTF_8)
    } catch (e: Exception) {
        Log.e("Nexus:Scan", "Failed to parse NDEF record", e)
        return null
    }
}

private fun ByteArray.toHex(): String = joinToString("") { "%02X".format(it) }

private data class ParsedVCard(
    val name: String = "",
    val jobTitle: String = "",
    val company: String = "",
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    val linkedin: String = "",
    val instagram: String = "",
    val twitter: String = "",
    val github: String = "",
    val organizationId: String = ""
)

private fun parseVCard(vcard: String): ParsedVCard {
    var name = ""
    var jobTitle = ""
    var company = ""
    var phone = ""
    var email = ""
    var website = ""
    var linkedin = ""
    var instagram = ""
    var twitter = ""
    var github = ""
    var organizationId = ""

    for (line in vcard.lines()) {
        val trimmed = line.trim()
        when {
            trimmed.startsWith("FN:") -> name = trimmed.removePrefix("FN:")
            trimmed.startsWith("TITLE:") -> jobTitle = trimmed.removePrefix("TITLE:")
            trimmed.startsWith("ORG:") -> company = trimmed.removePrefix("ORG:")
            trimmed.startsWith("TEL:") -> phone = trimmed.removePrefix("TEL:")
            trimmed.startsWith("EMAIL:") -> email = trimmed.removePrefix("EMAIL:")
            trimmed.startsWith("URL:") -> website = trimmed.removePrefix("URL:")
            trimmed.startsWith("X-NEXUS-ORG:") -> organizationId = trimmed.removePrefix("X-NEXUS-ORG:")
            trimmed.startsWith("ADR:") -> {
                // ADR:;;address;;;;
                val parts = trimmed.removePrefix("ADR:").split(";")
                // skip empty parts, join non-empty
            }
            trimmed.contains("type=linkedin", ignoreCase = true) -> {
                linkedin = trimmed.substringAfter(":")
            }
            trimmed.contains("type=instagram", ignoreCase = true) -> {
                instagram = trimmed.substringAfter(":")
            }
            trimmed.contains("type=twitter", ignoreCase = true) -> {
                twitter = trimmed.substringAfter(":")
            }
            trimmed.contains("type=github", ignoreCase = true) -> {
                github = trimmed.substringAfter(":")
            }
        }
    }

    return ParsedVCard(
        name = name, jobTitle = jobTitle, company = company,
        phone = phone, email = email, website = website,
        linkedin = linkedin, instagram = instagram,
        twitter = twitter, github = github,
        organizationId = organizationId
    )
}
