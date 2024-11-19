package com.example.cateredtoyou.services

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.cateredtoyou.apifiles.ApiConnect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Base64
import org.json.JSONObject

class TokenManager(context: Context) {

    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Save access and refresh tokens
    fun saveTokens(accessToken: String, refreshToken: String) {
        sharedPreferences.edit().apply {
            putString("access_token", accessToken)
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

    // Refresh the access token using the refresh token
    suspend fun refreshAccessToken(refreshToken: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Call the API to refresh the token
                val response = ApiConnect.refreshToken(refreshToken) //replace with correct call
                if (response.isSuccessful) {
                    val newAccessToken = response.body()?.accessToken
                    if (newAccessToken != null) {
                        // Save new tokens
                        saveTokens(newAccessToken, refreshToken)
                        return@withContext newAccessToken
                    }
                }
            } catch (e: Exception) {
                // Log or handle the error
                e.printStackTrace()  // Add logging here for debugging
            }
            return@withContext null
        }
    }

    // Check if the access token is expired and refresh it if necessary
    suspend fun getAccessTokenOrRefresh(): String? {
        val accessToken = getStoredAccessToken() ?: return null
        if (isTokenExpired(accessToken)) {
            // Token is expired, try to refresh it using the stored refresh token
            val refreshToken = getStoredRefreshToken() ?: return null
            return refreshAccessToken(refreshToken)
        }
        return accessToken
    }

    // Simple check for token expiration (this method may need to be adjusted to your app's token expiration logic)
    private fun isTokenExpired(accessToken: String): Boolean {
        // Example expiration check (you can adjust it based on your token structure)
        return System.currentTimeMillis() / 1000 > extractExpirationTime(accessToken)
    }

    // Decode the token to extract the expiration time (using JWT decoding)
    private fun extractExpirationTime(accessToken: String): Long {
        // Decode the JWT token (Base64 decode the payload)
        try {
            val parts = accessToken.split(".")
            if (parts.size == 3) {
                // Decode the payload (second part of JWT)
                val decodedBytes = android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT)
                val decodedString = String(decodedBytes)
                val json = JSONObject(decodedString)
                return json.optLong("exp", 0L)  // Return the expiration time in seconds
            }
        } catch (e: Exception) {
            e.printStackTrace()  // Add error handling for invalid token formats
        }
        return 0L  // Return 0 if the token is invalid or expiration is missing
    }
}
