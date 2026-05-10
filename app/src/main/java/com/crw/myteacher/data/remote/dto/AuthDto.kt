package com.crw.myteacher.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val role: String = "STUDENT"
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String? = null,
    val tokenType: String = "Bearer",
    val expiresIn: Long? = null
)

@Serializable
data class OAuth2CallbackDto(
    val code: String,
    val state: String? = null
)

