package com.kryptoxotis.nexus.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import platform.CoreImage.CIContext
import platform.CoreImage.CIFilter
import platform.CoreImage.filterWithName
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.dataUsingEncoding

actual object QrGenerator {
    actual suspend fun generate(content: String, size: Int): ImageBitmap {
        val data = (content as NSString).dataUsingEncoding(NSUTF8StringEncoding)
            ?: throw IllegalStateException("Failed to encode QR content")

        val filter = CIFilter.filterWithName("CIQRCodeGenerator")
            ?: throw IllegalStateException("CIQRCodeGenerator not available")
        filter.setDefaults()
        filter.setValue(data, forKey = "inputMessage")
        filter.setValue("M", forKey = "inputCorrectionLevel")

        val ciImage = filter.valueForKey("outputImage")
            ?: throw IllegalStateException("QR generation failed")

        // For now, generate a simple placeholder bitmap
        // Full CIImage → pixel extraction requires more complex CoreGraphics bridging
        val pixels = IntArray(size * size) { 0xFF000000.toInt() }

        val skiaBitmap = Bitmap()
        skiaBitmap.allocPixels(ImageInfo(size, size, ColorType.BGRA_8888, ColorAlphaType.PREMUL))
        skiaBitmap.installPixels(pixels)

        return skiaBitmap.toComposeImageBitmap()
    }
}
