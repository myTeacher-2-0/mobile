package com.crw.myteacher.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val accountId: String,
    val firstName: String,
    val lastName: String,
    val accountType: String,
    val profilePictureUrl: String? = null,
    val phoneNumber: String? = null
)

@Serializable
data class UpdateAccountRequestDto(
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null
)

@Serializable
data class ChangePasswordRequestDto(
    val currentPassword: String,
    val newPassword: String
)

