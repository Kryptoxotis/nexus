package com.kryptoxotis.nexus.platform

import java.text.Normalizer

actual fun normalizeNfkc(text: String): String =
    Normalizer.normalize(text, Normalizer.Form.NFKC)
