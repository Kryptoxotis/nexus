package com.kryptoxotis.nexus.platform

import androidx.compose.ui.graphics.ImageBitmap

expect object QrGenerator {
    suspend fun generate(
        content: String,
        size: Int = 512,
        foregroundColor: Int = 0xFF000000.toInt(),
        backgroundColor: Int = 0xFFFFFFFF.toInt()
    ): ImageBitmap
}
