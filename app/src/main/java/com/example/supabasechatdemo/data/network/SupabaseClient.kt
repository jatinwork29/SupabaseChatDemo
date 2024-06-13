package com.example.supabasechatdemo.data.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseClient {
    private const val supabaseUrl =
        "https://snqazndhhmtpvindqdhe.supabase.co" // Replace with your Supabase project URL
    private const val supabaseAnonKey =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNucWF6bmRoaG10cHZpbmRxZGhlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTgxNzU5MDksImV4cCI6MjAzMzc1MTkwOX0.9oUmNfJRPp-VovF7-ocPHNV_XFoBqtnlrVUFb32b77I" // Replace with your Supabase Anon key (optional)

    // Creating supabase client
    val supabaseClient = createSupabaseClient(
        supabaseUrl = supabaseUrl,
        supabaseKey = supabaseAnonKey
    ) {
        install(Auth)
        install(Postgrest)
        install(Realtime)
    }
}