package com.crw.myteacher.push

import kotlinx.serialization.Serializable

/**
 * Request do rejestracji tokenu FCM na backendzie.
 * Przygotowane na przyszłą integrację z Firebase Cloud Messaging.
 */
@Serializable
data class FcmTokenRequest(
    val token: String,
    val platform: String = "android"
)

