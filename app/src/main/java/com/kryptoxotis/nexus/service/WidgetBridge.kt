package com.kryptoxotis.nexus.service

import android.content.Context

/**
 * SharedPrefs bridge so the widget process can read the current userId
 * without needing Supabase auth context.
 */
object WidgetBridge {
    private const val PREFS = "nexus_widget_bridge"
    private const val KEY_USER_ID = "user_id"

    fun writeUserId(context: Context, userId: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().apply {
                if (userId != null) putString(KEY_USER_ID, userId)
                else remove(KEY_USER_ID)
            }.apply()
    }

    fun readUserId(context: Context): String? {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_USER_ID, null)
    }
}
