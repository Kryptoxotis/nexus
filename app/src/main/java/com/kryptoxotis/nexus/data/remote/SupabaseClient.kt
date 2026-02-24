package com.kryptoxotis.nexus.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.runBlocking

object SupabaseClientProvider {

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
            install(Storage)
        }
    }

    fun resetClient() {
        synchronized(this) {
            try { runBlocking { client?.close() } } catch (_: Exception) {}
            client = null
        }
    }
}
