package com.readllm.app.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure storage for OAuth tokens using Android's EncryptedSharedPreferences
 * 
 * This ensures tokens are encrypted at rest and protected from unauthorized access.
 */
class SecureTokenStorage(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "github_auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER_LOGIN = "user_login"
    }
    
    /**
     * Save access token securely
     */
    fun saveAccessToken(token: String) {
        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .apply()
    }
    
    /**
     * Get access token
     */
    fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }
    
    /**
     * Save user login
     */
    fun saveUserLogin(login: String) {
        sharedPreferences.edit()
            .putString(KEY_USER_LOGIN, login)
            .apply()
    }
    
    /**
     * Get user login
     */
    fun getUserLogin(): String? {
        return sharedPreferences.getString(KEY_USER_LOGIN, null)
    }
    
    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        return getAccessToken() != null
    }
    
    /**
     * Clear all auth data
     */
    fun clearAuth() {
        sharedPreferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_USER_LOGIN)
            .apply()
    }
}
