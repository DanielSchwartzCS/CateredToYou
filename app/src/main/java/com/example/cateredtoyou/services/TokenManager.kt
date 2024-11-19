package com.example.cateredtoyou.services

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {

    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Save access and refresh tokens
    fun saveTokens(token: String, refreshToken: String) {
        sharedPreferences.edit().apply {
            putString("access_token", token)
            putString("refresh_token", refreshToken)
            apply()
        }
    }

    // Retrieve the stored access token
    fun getStoredAccessToken(): String? {
        return sharedPreferences.getString("access_token", null)
    }

    // Retrieve the stored refresh token
    fun getStoredRefreshToken(): String? {
        return sharedPreferences.getString("refresh_token", null)
    }

    // Clear all stored tokens
    fun clearStoredTokens() {
        sharedPreferences.edit().clear().apply()
    }
}
