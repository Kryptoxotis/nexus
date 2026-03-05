package com.kryptoxotis.nexus.presentation.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kryptoxotis.nexus.domain.model.CardType
import com.kryptoxotis.nexus.platform.NfcManager
import com.kryptoxotis.nexus.platform.openUrl
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanCardScreen(
    receivedCardViewModel: ReceivedCardViewModel,
    personalCardViewModel: PersonalCardViewModel? = null,
    nfcManager: NfcManager? = null,
    onNavigateBack: () -> Unit
) {
    var scanResult by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(true) }
    var scanError by remember { mutableStateOf<String?>(null) }

    // Start NFC reading via expect/actual NfcManager
    LaunchedEffect(Unit) {
        if (nfcManager == null || !nfcManager.isSupported()) {
            scanError = "NFC is not available on this device"
            isScanning = false
            return@LaunchedEffect
        }
        try {
            val result = nfcManager.readNdef()
            if (result != null) {
                scanResult = result
                isScanning = false
            } else {
                scanError = "Could not read card data"
                isScanning = false
            }
        } catch (e: Exception) {
            scanError = "Scan failed: ${e.message}"
            isScanning = false
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
            } else if (scanError != null) {
                Icon(
                    Icons.Default.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = scanError!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onNavigateBack) {
                    Text("Go Back")
                }
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
                    text = if (isVCard) "Nexus received!" else "Card received!",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                if (isVCard && result != null) {
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

                    // Clickable links for each non-empty field
                    Spacer(modifier = Modifier.height(12.dp))
                    val linkItems = listOfNotNull(
                        if (parsed.phone.isNotBlank()) Triple("Phone", parsed.phone, "tel:${parsed.phone}") else null,
                        if (parsed.email.isNotBlank()) Triple("Email", parsed.email, "mailto:${parsed.email}") else null,
                        if (parsed.website.isNotBlank()) Triple("Website", parsed.website, parsed.website) else null,
                        if (parsed.instagram.isNotBlank()) Triple("Instagram", parsed.instagram, parsed.instagram) else null,
                        if (parsed.twitter.isNotBlank()) Triple("X", parsed.twitter, parsed.twitter) else null,
                        if (parsed.github.isNotBlank()) Triple("GitHub", parsed.github, parsed.github) else null,
                        if (parsed.linkedin.isNotBlank()) Triple("LinkedIn", parsed.linkedin, parsed.linkedin) else null,
                        if (parsed.facebook.isNotBlank()) Triple("Facebook", parsed.facebook, parsed.facebook) else null,
                        if (parsed.youtube.isNotBlank()) Triple("YouTube", parsed.youtube, parsed.youtube) else null,
                        if (parsed.tiktok.isNotBlank()) Triple("TikTok", parsed.tiktok, parsed.tiktok) else null,
                        if (parsed.discord.isNotBlank()) Triple("Discord", parsed.discord, parsed.discord) else null,
                        if (parsed.twitch.isNotBlank()) Triple("Twitch", parsed.twitch, parsed.twitch) else null,
                        if (parsed.whatsapp.isNotBlank()) Triple(
                            "WhatsApp", parsed.whatsapp,
                            "https://wa.me/${parsed.whatsapp.replace(Regex("[^0-9+]"), "")}"
                        ) else null
                    )
                    linkItems.forEach { (label, display, uri) ->
                        TextButton(onClick = {
                            try {
                                openUrl(uri)
                            } catch (_: Exception) {
                            }
                        }) {
                            Text(
                                "$label: $display",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
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
                                github = parsed.github,
                                facebook = parsed.facebook,
                                youtube = parsed.youtube,
                                tiktok = parsed.tiktok,
                                discord = parsed.discord,
                                twitch = parsed.twitch,
                                whatsapp = parsed.whatsapp
                            )
                            contactSaved = true
                        }) {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Nexus")
                        }
                    } else {
                        LaunchedEffect(Unit) {
                            delay(800)
                            onNavigateBack()
                        }
                        Text(
                            text = "Nexus saved!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (result != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    val isUrl = result.trim().let {
                        it.startsWith("http://") || it.startsWith("https://")
                    }
                    if (isUrl) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            try {
                                openUrl(result)
                            } catch (_: Exception) {
                            }
                        }) {
                            Icon(
                                Icons.Default.OpenInBrowser,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
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
                                val cardType =
                                    if (isUrl) CardType.LINK else CardType.CUSTOM
                                val cardTitle = if (isUrl) {
                                    result.removePrefix("https://")
                                        .removePrefix("http://")
                                        .removePrefix("www.")
                                        .substringBefore("/")
                                } else {
                                    result.take(50)
                                }
                                personalCardViewModel.addCard(
                                    cardType = cardType,
                                    title = cardTitle,
                                    content = result
                                )
                                walletSaved = true
                            }) {
                                Icon(
                                    Icons.Default.AddCard,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
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
            }
        }
    }
}

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
    val facebook: String = "",
    val youtube: String = "",
    val tiktok: String = "",
    val discord: String = "",
    val twitch: String = "",
    val whatsapp: String = "",
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
    var facebook = ""
    var youtube = ""
    var tiktok = ""
    var discord = ""
    var twitch = ""
    var whatsapp = ""
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
            trimmed.startsWith("ADR:") -> { /* skip */ }
            trimmed.contains("type=linkedin", ignoreCase = true) -> linkedin = trimmed.substringAfter(":")
            trimmed.contains("type=instagram", ignoreCase = true) -> instagram = trimmed.substringAfter(":")
            trimmed.contains("type=twitter", ignoreCase = true) -> twitter = trimmed.substringAfter(":")
            trimmed.contains("type=github", ignoreCase = true) -> github = trimmed.substringAfter(":")
            trimmed.contains("type=facebook", ignoreCase = true) -> facebook = trimmed.substringAfter(":")
            trimmed.contains("type=youtube", ignoreCase = true) -> youtube = trimmed.substringAfter(":")
            trimmed.contains("type=tiktok", ignoreCase = true) -> tiktok = trimmed.substringAfter(":")
            trimmed.contains("type=discord", ignoreCase = true) -> discord = trimmed.substringAfter(":")
            trimmed.contains("type=twitch", ignoreCase = true) -> twitch = trimmed.substringAfter(":")
            trimmed.contains("type=whatsapp", ignoreCase = true) -> whatsapp = trimmed.substringAfter(":")
        }
    }

    return ParsedVCard(
        name = name, jobTitle = jobTitle, company = company,
        phone = phone, email = email, website = website,
        linkedin = linkedin, instagram = instagram,
        twitter = twitter, github = github,
        facebook = facebook, youtube = youtube,
        tiktok = tiktok, discord = discord,
        twitch = twitch, whatsapp = whatsapp,
        organizationId = organizationId
    )
}
