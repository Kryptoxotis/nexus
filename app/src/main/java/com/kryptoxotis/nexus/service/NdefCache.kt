package com.kryptoxotis.nexus.service

import android.content.Context
import android.util.Base64
import android.util.Log
import com.kryptoxotis.nexus.domain.model.BusinessCardData
import com.kryptoxotis.nexus.domain.model.CardType
import com.kryptoxotis.nexus.domain.model.PersonalCard

/**
 * Writes pre-built NDEF bytes to SharedPreferences so the HCE service
 * can read them instantly (~1ms) without hitting Room or needing auth context.
 */
object NdefCache {
    private const val PREFS_NAME = "nexus_ndef_cache"
    private const val KEY_NDEF_BYTES = "ndef_bytes"
    private const val TAG = "Nexus:NdefCache"

    fun write(context: Context, card: PersonalCard?) {
        val bytes = if (card != null) {
            when (card.cardType) {
                CardType.LINK, CardType.FILE, CardType.SOCIAL_MEDIA -> {
                    NFCPassService.createNdefMessage(card.content ?: card.title, isUri = true)
                }
                CardType.BUSINESS_CARD -> {
                    val vcard = BusinessCardData.fromJson(card.content ?: "").toVCard()
                    NFCPassService.createNdefMessage(vcard, isUri = false)
                }
                else -> {
                    NFCPassService.createNdefMessage(card.content ?: card.id, isUri = false)
                }
            }
        } else {
            null
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (bytes != null) {
            prefs.edit().putString(KEY_NDEF_BYTES, Base64.encodeToString(bytes, Base64.NO_WRAP)).apply()
            Log.d(TAG, "Cached ${bytes.size} NDEF bytes")
        } else {
            prefs.edit().remove(KEY_NDEF_BYTES).apply()
            Log.d(TAG, "Cleared NDEF cache")
        }
    }

    fun read(context: Context): ByteArray? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encoded = prefs.getString(KEY_NDEF_BYTES, null) ?: return null
        return try {
            Base64.decode(encoded, Base64.NO_WRAP)
        } catch (_: Exception) {
            null
        }
    }
}
