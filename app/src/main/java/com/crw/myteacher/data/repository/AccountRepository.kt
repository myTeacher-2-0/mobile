package com.crw.myteacher.data.repository

import com.crw.myteacher.data.remote.MyTeacherApi
import com.crw.myteacher.data.remote.dto.UserDto

class AccountRepository(private val api: MyTeacherApi) : BaseRepository() {

    suspend fun getCurrentUser(): Result<UserDto> {
        return safeApiCall { api.getCurrentUser() }
    }

    suspend fun getUserById(userId: Long): Result<UserDto> {
        return safeApiCall { api.getUserById(userId) }
    }
}

