package com.nfcpass.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

/**
 * Supabase client singleton.
 * Configure SUPABASE_URL and SUPABASE_ANON_KEY before use.
 */
object SupabaseClientProvider {

    // These should be set from BuildConfig or secrets before first use
    var supabaseUrl: String = ""
    var supabaseAnonKey: String = ""

    @Volatile
    private var client: SupabaseClient? = null

    fun getClient(): SupabaseClient {
        return client ?: synchronized(this) {
            client ?: createClient().also { client = it }
        }
    }

    private fun createClient(): SupabaseClient {
        require(supabaseUrl.isNotBlank()) { "Supabase URL not configured" }
        require(supabaseAnonKey.isNotBlank()) { "Supabase Anon Key not configured" }

        return createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseAnonKey
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    }

    /**
     * Resets the client (used when switching accounts).
     */
    fun resetClient() {
        synchronized(this) {
            client = null
        }
    }
}
