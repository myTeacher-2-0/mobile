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
        val path = originalRequest.url.encodedPath
        val method = originalRequest.method

        // Nie dodawaj tokena do endpointów logowania/rejestracji
        val isAuthEndpoint = AUTH_PATHS.any { path.endsWith(it) }

        val token = tokenManager.accessToken
        val hasToken = !token.isNullOrBlank()

        val request = if (!isAuthEndpoint && hasToken) {
            Log.d(TAG, "→ $method $path [token: ${token.take(15)}...]")
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("X-Client-Platform", "android")
                .build()
        } else {
            Log.d(TAG, "→ $method $path [no token sent, isAuthEndpoint=$isAuthEndpoint, hasToken=$hasToken]")
            originalRequest
        }

        val response = chain.proceed(request)
        val code = response.code
        Log.d(TAG, "← $method $path → HTTP $code")

        // Jeśli serwer zwrócił 401 lub 403 i nie jest to endpoint auth — sesja wygasła
        // Reagujemy TYLKO gdy token był wysłany (użytkownik był zalogowany)
        // NIE czyścimy tokenu gdy suppressExpiredEvent=true (np. walidacja sesji, ładowanie dashboardu)
        if (!isAuthEndpoint && hasToken && (code == 401 || code == 403)) {
            Log.w(TAG, "⚠ 401/403 received for $method $path, suppressExpiredEvent=${SessionManager.suppressExpiredEvent}")
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
