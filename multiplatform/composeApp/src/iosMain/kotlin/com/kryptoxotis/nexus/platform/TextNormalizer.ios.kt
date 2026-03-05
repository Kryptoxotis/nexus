package com.kryptoxotis.nexus.platform

import platform.Foundation.NSString
import platform.Foundation.precomposedStringWithCompatibilityMapping

actual fun normalizeNfkc(text: String): String {
    @Suppress("CAST_NEVER_SUCCEEDS")
    val nsString = text as NSString
    return nsString.precomposedStringWithCompatibilityMapping
}
