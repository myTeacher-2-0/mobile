package com.crw.myteacher.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"
        private val AUTH_PATHS = listOf("/api/auth/login")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url
        val path = url.encodedPath
        val method = originalRequest.method
        val isAuthEndpoint = AUTH_PATHS.any { path.endsWith(it) }

        val token = tokenManager.accessToken
        val hasToken = !token.isNullOrBlank()

        val request = if (!isAuthEndpoint && hasToken) {
            Log.d(TAG, "→ $method $url [token: ${token.take(15)}...]")
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("X-Client-Platform", "android")
                .build()
        } else {
            Log.d(TAG, "→ $method $url [no token sent, isAuthEndpoint=$isAuthEndpoint, hasToken=$hasToken]")
            originalRequest
        }

        Log.d(TAG, "── REQUEST HEADERS for $method $path ──")
        request.headers.forEach { (name, value) ->
            val logValue = if (name.equals("Authorization", ignoreCase = true)) {
                value.take(25) + "..."
            } else {
                value
            }
            Log.d(TAG, "  $name: $logValue")
        }

        val response = chain.proceed(request)
        val code = response.code

        Log.d(TAG, "── RESPONSE for $method $path ──")
        Log.d(TAG, "← $method $url → HTTP $code")
        Log.d(TAG, "  Protocol: ${response.protocol}")
        Log.d(TAG, "  Message: ${response.message}")
        Log.d(TAG, "  isRedirect: ${response.isRedirect}")
        Log.d(TAG, "  priorResponse: ${response.priorResponse?.code}")
        Log.d(TAG, "  networkResponse: ${response.networkResponse?.code}")
        Log.d(TAG, "  cacheResponse: ${response.cacheResponse?.code}")
        response.headers.forEach { (name, value) ->
            Log.d(TAG, "  [H] $name: $value")
        }

        if (!isAuthEndpoint && hasToken && (code == 401 || code == 403)) {
            Log.w(TAG, "⚠ 401/403 received for $method $url")
            Log.w(TAG, "⚠ Token used (first 20 chars): ${token.take(20)}")
            Log.w(TAG, "⚠ suppressExpiredEvent=${SessionManager.suppressExpiredEvent}")

            val peekBody = response.peekBody(1024)
            Log.w(TAG, "⚠ Response body: ${peekBody.string()}")

            if (!SessionManager.suppressExpiredEvent) {
                Log.w(TAG, "⚠ SESSION EXPIRED — clearing token and notifying")
                tokenManager.clear()
                SessionManager.notifySessionExpired()
            } else {
                Log.w(TAG, "⚠ suppressExpiredEvent=true — token NOT cleared, event NOT emitted")
            }
        }

        return response
    }
}
