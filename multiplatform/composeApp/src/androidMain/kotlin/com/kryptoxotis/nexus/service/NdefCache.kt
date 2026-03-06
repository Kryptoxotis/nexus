package com.kryptoxotis.nexus.service

import android.content.Context
import android.util.Base64
import com.kryptoxotis.nexus.domain.model.BusinessCardData
import com.kryptoxotis.nexus.domain.model.CardType
import com.kryptoxotis.nexus.domain.model.PersonalCard

object NdefCache {
    private const val PREFS_NAME = "nexus_ndef_cache"
    private const val KEY_NDEF_BYTES = "ndef_bytes"

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
        } else {
            prefs.edit().remove(KEY_NDEF_BYTES).apply()
        }
    }

    fun writeUri(context: Context, uri: String) {
        val bytes = NFCPassService.createNdefMessage(uri, isUri = true)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_NDEF_BYTES, Base64.encodeToString(bytes, Base64.NO_WRAP)).apply()
    }

    fun writeVCard(context: Context, vcard: String) {
        val bytes = NFCPassService.createNdefMessage(vcard, isUri = false)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_NDEF_BYTES, Base64.encodeToString(bytes, Base64.NO_WRAP)).apply()
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
