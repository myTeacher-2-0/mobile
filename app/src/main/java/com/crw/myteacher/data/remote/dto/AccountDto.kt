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
