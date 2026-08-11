package com.kryptoxotis.nexus.presentation.profile

import android.content.Context
import android.content.SharedPreferences

/**
 * Local UI preferences. Stored on-device only — none of these sync.
 * Appearance and card view are recorded now and take visual effect
 * when their features land (light theme, deck view).
 */
object SettingsStore {
    private const val PREFS = "nexus_ui_settings"

    const val APPEARANCE_DARK = "dark"
    const val APPEARANCE_LIGHT = "light"
    const val CARD_VIEW_LIST = "list"
    const val CARD_VIEW_DECK = "deck"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun appearance(context: Context): String =
        prefs(context).getString("appearance", APPEARANCE_DARK) ?: APPEARANCE_DARK

    fun setAppearance(context: Context, value: String) =
        prefs(context).edit().putString("appearance", value).apply()

    fun cardView(context: Context): String =
        prefs(context).getString("card_view", CARD_VIEW_LIST) ?: CARD_VIEW_LIST

    fun setCardView(context: Context, value: String) =
        prefs(context).edit().putString("card_view", value).apply()

    fun nfcSharing(context: Context): Boolean =
        prefs(context).getBoolean("nfc_sharing", true)

    fun setNfcSharing(context: Context, value: Boolean) =
        prefs(context).edit().putBoolean("nfc_sharing", value).apply()

    fun notifications(context: Context): Boolean =
        prefs(context).getBoolean("notifications", true)

    fun setNotifications(context: Context, value: Boolean) =
        prefs(context).edit().putBoolean("notifications", value).apply()
}
