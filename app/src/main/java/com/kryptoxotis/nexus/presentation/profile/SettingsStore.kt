package com.kryptoxotis.nexus.presentation.profile

import android.content.Context
import android.content.SharedPreferences
import com.kryptoxotis.nexus.presentation.theme.NexusAppearance

/**
 * Local UI preferences, persisted on-device and mirrored into
 * [NexusAppearance] snapshot state so the UI reacts instantly.
 */
object SettingsStore {
    private const val PREFS = "nexus_ui_settings"

    const val APPEARANCE_DARK = "dark"
    const val APPEARANCE_LIGHT = "light"
    const val CARD_VIEW_LIST = "list"
    const val CARD_VIEW_DECK = "deck"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Load persisted values into live state. Call once from MainActivity. */
    fun init(context: Context) {
        val p = prefs(context)
        NexusAppearance.dark = (p.getString("appearance", APPEARANCE_DARK) ?: APPEARANCE_DARK) == APPEARANCE_DARK
        NexusAppearance.deckView = (p.getString("card_view", CARD_VIEW_LIST) ?: CARD_VIEW_LIST) == CARD_VIEW_DECK
        NexusAppearance.nfcSharing = p.getBoolean("nfc_sharing", true)
        NexusAppearance.notifications = p.getBoolean("notifications", true)
    }

    fun appearance(context: Context): String =
        if (NexusAppearance.dark) APPEARANCE_DARK else APPEARANCE_LIGHT

    fun setAppearance(context: Context, value: String) {
        NexusAppearance.dark = value == APPEARANCE_DARK
        prefs(context).edit().putString("appearance", value).apply()
    }

    fun cardView(context: Context): String =
        if (NexusAppearance.deckView) CARD_VIEW_DECK else CARD_VIEW_LIST

    fun setCardView(context: Context, value: String) {
        NexusAppearance.deckView = value == CARD_VIEW_DECK
        prefs(context).edit().putString("card_view", value).apply()
    }

    fun nfcSharing(context: Context): Boolean = NexusAppearance.nfcSharing

    fun setNfcSharing(context: Context, value: Boolean) {
        NexusAppearance.nfcSharing = value
        prefs(context).edit().putBoolean("nfc_sharing", value).apply()
    }

    fun notifications(context: Context): Boolean = NexusAppearance.notifications

    fun setNotifications(context: Context, value: Boolean) {
        NexusAppearance.notifications = value
        prefs(context).edit().putBoolean("notifications", value).apply()
    }
}
