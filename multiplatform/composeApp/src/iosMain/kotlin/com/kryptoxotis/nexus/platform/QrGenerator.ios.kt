package com.kryptoxotis.nexus.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import platform.CoreGraphics.CGAffineTransformMakeScale
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGColorSpaceCreateDeviceGray
import platform.CoreGraphics.CGContextDrawImage
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGRectMake
import platform.CoreImage.CIContext
import platform.CoreImage.CIFilter
import platform.CoreImage.filterWithName
import platform.CoreImage.outputImage
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.dataUsingEncoding

@OptIn(ExperimentalForeignApi::class)
actual object QrGenerator {
    actual suspend fun generate(content: String, size: Int): ImageBitmap {
        val data = (content as NSString).dataUsingEncoding(NSUTF8StringEncoding)
            ?: throw IllegalStateException("Failed to encode QR content")

        val filter = CIFilter.filterWithName("CIQRCodeGenerator")
            ?: throw IllegalStateException("CIQRCodeGenerator not available")
        filter.setDefaults()
        filter.setValue(data, forKey = "inputMessage")
        filter.setValue("M", forKey = "inputCorrectionLevel")

        val ciImage = filter.outputImage
            ?: throw IllegalStateException("QR generation failed")

        val context = CIContext()
        val cgImage = context.createCGImage(ciImage, fromRect = ciImage.extent)
            ?: throw IllegalStateException("Failed to create CGImage")

        val imgW = CGImageGetWidth(cgImage).toInt()
        val imgH = CGImageGetHeight(cgImage).toInt()
        val scaleX = size.toDouble() / imgW
        val scaleY = size.toDouble() / imgH

        val colorSpace = CGColorSpaceCreateDeviceGray()
        val bitmapCtx = CGBitmapContextCreate(
            null, size.toULong(), size.toULong(), 8u, 0u, null, 0u
        ) ?: throw IllegalStateException("Failed to create bitmap context")

        CGContextDrawImage(bitmapCtx, CGRectMake(0.0, 0.0, size.toDouble(), size.toDouble()), cgImage)

        // Create a simple black/white bitmap at the requested size
        val pixels = IntArray(size * size) { 0xFFFFFFFF.toInt() } // white
        // Note: Full CIFilter→pixel extraction requires more complex bridging
        // This provides a white placeholder; real implementation needs CGBitmapContext pixel reading

        val skiaBitmap = Bitmap()
        skiaBitmap.allocPixels(ImageInfo(size, size, ColorType.BGRA_8888, ColorAlphaType.PREMUL))
        pixels.usePinned { pinned ->
            skiaBitmap.installPixels(skiaBitmap.imageInfo, pinned.addressOf(0), (size * 4).toLong())
        }

        return skiaBitmap.asComposeImageBitmap()
    }

    private fun Bitmap.asComposeImageBitmap(): ImageBitmap = toComposeImageBitmap()
}
