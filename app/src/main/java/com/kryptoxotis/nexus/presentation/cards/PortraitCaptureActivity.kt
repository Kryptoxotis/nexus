package com.kryptoxotis.nexus.presentation.cards

import com.journeyapps.barcodescanner.CaptureActivity

/**
 * The stock zxing CaptureActivity opens landscape; this subclass is pinned
 * portrait in the manifest so the scanner matches how the phone is held.
 */
class PortraitCaptureActivity : CaptureActivity()
