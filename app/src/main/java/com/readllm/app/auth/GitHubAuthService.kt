package com.readllm.app.auth

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.readllm.app.BuildConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.*
import kotlin.coroutines.resume

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
    
    private val secureStorage = SecureTokenStorage(context)
    
    companion object {
        // GitHub OAuth endpoints
        private const val AUTHORIZATION_ENDPOINT = "https://github.com/login/oauth/authorize"
        private const val TOKEN_ENDPOINT = "https://github.com/login/oauth/access_token"
        
        // GitHub OAuth Client ID - read from BuildConfig (configured in local.properties)
        // To configure: add GITHUB_CLIENT_ID=your_client_id to local.properties
        // Get your Client ID from: https://github.com/settings/developers
        private val CLIENT_ID = BuildConfig.GITHUB_CLIENT_ID
        private const val REDIRECT_URI = "com.readllm.app://oauth"
        
        // Scopes needed for GitHub Models API
        private const val SCOPES = "read:user"
    }
    
    private val serviceConfig = AuthorizationServiceConfiguration(
        android.net.Uri.parse(AUTHORIZATION_ENDPOINT),
        android.net.Uri.parse(TOKEN_ENDPOINT)
    )
    
    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        return secureStorage.isAuthenticated()
    }
    
    /**
     * Get access token
     */
    fun getAccessToken(): String? {
        return secureStorage.getAccessToken()
    }
    
    /**
     * Get user login
     */
    fun getUserLogin(): String? {
        return secureStorage.getUserLogin()
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
            
            // Use suspendCancellableCoroutine to properly await callback
            val tokenResponse = suspendCancellableCoroutine<TokenResponse> { continuation ->
                authService.performTokenRequest(tokenRequest) { resp, ex ->
                    if (ex != null) {
                        continuation.cancel(ex)
                    } else if (resp != null) {
                        continuation.resume(resp)
                    } else {
                        continuation.cancel(Exception("No token response received"))
                    }
                }
            }
            
            val token = tokenResponse.accessToken ?: return Result.failure(Exception("No access token"))
            
            // Store token securely
            secureStorage.saveAccessToken(token)
            
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Save access token and user info
     */
    fun saveAuth(token: String, userLogin: String) {
        secureStorage.saveAccessToken(token)
        secureStorage.saveUserLogin(userLogin)
    }
    
    /**
     * Clear authentication
     */
    fun clearAuth() {
        secureStorage.clearAuth()
    }
}
