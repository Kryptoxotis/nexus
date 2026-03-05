package com.kryptoxotis.nexus.platform

import platform.Foundation.NSString
import platform.Foundation.decomposedStringWithCompatibilityMapping
import platform.Foundation.precomposedStringWithCompatibilityMapping

actual fun normalizeNfkc(text: String): String {
    val nsString = NSString.create(string = text)
    return nsString.precomposedStringWithCompatibilityMapping
}
