package com.kryptoxotis.nexus.platform

import androidx.compose.ui.graphics.ImageBitmap

expect object QrGenerator {
    suspend fun generate(content: String, size: Int = 512): ImageBitmap
}
