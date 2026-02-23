package com.readllm.app.auth

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.openid.appauth.*

/**
 * GitHub OAuth Service
 * 
 * Handles GitHub OAuth authentication for accessing GitHub Models API.
 * GitHub Models provides free access to various LLMs including GPT-4, Llama, etc.
 * 
 * Setup instructions:
 * 1. Create a GitHub OAuth App at https://github.com/settings/developers
 * 2. Set callback URL to: com.readllm.app://oauth
 * 3. Add your Client ID to this file
 * 
 * Benefits over on-device models:
 * - Access to state-of-the-art models (GPT-4, Claude, Llama 3, etc.)
 * - No local model download required (saves ~2-3 GB)
 * - Better question quality and answer evaluation
 * - Free tier available through GitHub Models
 */
class GitHubAuthService(private val context: Context) {
    
    private val Context.dataStore by preferencesDataStore(name = "github_auth")
    
    companion object {
        // GitHub OAuth endpoints
        private const val AUTHORIZATION_ENDPOINT = "https://github.com/login/oauth/authorize"
        private const val TOKEN_ENDPOINT = "https://github.com/login/oauth/access_token"
        
        // TODO: Replace with your GitHub OAuth App Client ID
        // Get this from: https://github.com/settings/developers
        private const val CLIENT_ID = "YOUR_GITHUB_CLIENT_ID"
        private const val REDIRECT_URI = "com.readllm.app://oauth"
        
        // Scopes needed for GitHub Models API
        private const val SCOPES = "read:user"
        
        // DataStore keys
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("github_access_token")
        private val USER_LOGIN_KEY = stringPreferencesKey("github_user_login")
    }
    
    private val serviceConfig = AuthorizationServiceConfiguration(
        android.net.Uri.parse(AUTHORIZATION_ENDPOINT),
        android.net.Uri.parse(TOKEN_ENDPOINT)
    )
    
    /**
     * Get stored access token
     */
    val accessToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN_KEY]
    }
    
    /**
     * Get stored user login
     */
    val userLogin: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_LOGIN_KEY]
    }
    
    /**
     * Check if user is authenticated
     */
    suspend fun isAuthenticated(): Boolean {
        var authenticated = false
        context.dataStore.data.collect { preferences ->
            authenticated = preferences[ACCESS_TOKEN_KEY] != null
        }
        return authenticated
    }
    
    /**
     * Build authorization request
     */
    fun buildAuthorizationRequest(): AuthorizationRequest {
        return AuthorizationRequest.Builder(
            serviceConfig,
            CLIENT_ID,
            ResponseTypeValues.CODE,
            android.net.Uri.parse(REDIRECT_URI)
        )
            .setScopes(SCOPES)
            .build()
    }
    
    /**
     * Start OAuth flow
     * Call this from your Activity/Fragment
     */
    fun startAuthFlow(
        activity: ComponentActivity,
        launcher: ActivityResultLauncher<Intent>
    ) {
        val authService = AuthorizationService(activity)
        val authRequest = buildAuthorizationRequest()
        val authIntent = authService.getAuthorizationRequestIntent(authRequest)
        launcher.launch(authIntent)
    }
    
    /**
     * Handle OAuth response and exchange code for token
     */
    suspend fun handleAuthResponse(
        response: AuthorizationResponse?,
        exception: AuthorizationException?
    ): Result<String> {
        if (exception != null) {
            return Result.failure(exception)
        }
        
        if (response == null) {
            return Result.failure(Exception("No authorization response"))
        }
        
        return try {
            // Exchange authorization code for access token
            val authService = AuthorizationService(context)
            val tokenRequest = response.createTokenExchangeRequest()
            
            // This is a blocking call - should be called from coroutine
            var tokenResponse: TokenResponse? = null
            var error: AuthorizationException? = null
            
            authService.performTokenRequest(tokenRequest) { resp, ex ->
                tokenResponse = resp
                error = ex
            }
            
            // Wait for response (in real implementation, use suspendCancellableCoroutine)
            while (tokenResponse == null && error == null) {
                kotlinx.coroutines.delay(100)
            }
            
            if (error != null) {
                return Result.failure(error!!)
            }
            
            val token = tokenResponse?.accessToken ?: return Result.failure(Exception("No access token"))
            
            // Store token
            context.dataStore.edit { preferences ->
                preferences[ACCESS_TOKEN_KEY] = token
            }
            
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Save access token and user info
     */
    suspend fun saveAuth(token: String, userLogin: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = token
            preferences[USER_LOGIN_KEY] = userLogin
        }
    }
    
    /**
     * Clear authentication
     */
    suspend fun clearAuth() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(USER_LOGIN_KEY)
        }
    }
    
    /**
     * Get current access token (suspend function)
     */
    suspend fun getAccessToken(): String? {
        var token: String? = null
        context.dataStore.data.collect { preferences ->
            token = preferences[ACCESS_TOKEN_KEY]
        }
        return token
    }
}
