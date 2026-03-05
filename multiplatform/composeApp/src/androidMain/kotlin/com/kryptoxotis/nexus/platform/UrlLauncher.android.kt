package com.kryptoxotis.nexus.platform

import android.content.Context
import android.content.Intent
import android.net.Uri

object UrlLauncherContext {
    lateinit var appContext: Context
}

actual fun openUrl(url: String) {
    val blockedSchemes = listOf("javascript:", "data:", "file:", "content:", "intent:", "blob:", "vbscript:")
    val trimmed = url.trim()
    if (blockedSchemes.any { trimmed.lowercase().startsWith(it) }) return
    val fixedUrl = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")
        && !trimmed.startsWith("tel:") && !trimmed.startsWith("mailto:")) "https://$trimmed" else trimmed
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fixedUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        UrlLauncherContext.appContext.startActivity(intent)
    } catch (_: Exception) { }
}
