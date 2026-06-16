package com.crw.myteacher.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    companion object {
        private val AUTH_PATHS = listOf("/api/auth/login")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath
        val isAuthEndpoint = AUTH_PATHS.any { path.endsWith(it) }

        val token = tokenManager.accessToken
        val hasToken = !token.isNullOrBlank()

        val request = if (!isAuthEndpoint && hasToken) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("X-Client-Platform", "android")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(request)
        val code = response.code

        if (!isAuthEndpoint && hasToken && (code == 401 || code == 403)) {
            if (!SessionManager.suppressExpiredEvent) {
                tokenManager.clear()
                SessionManager.notifySessionExpired()
            }
        }

        return response
    }
}
