package com.crw.myteacher.data.repository

import com.crw.myteacher.data.remote.MyTeacherApi
import com.crw.myteacher.data.remote.dto.ChangePasswordRequestDto
import com.crw.myteacher.data.remote.dto.UpdateAccountRequestDto
import com.crw.myteacher.data.remote.dto.UserDto

class AccountRepository(private val api: MyTeacherApi) : BaseRepository() {

    suspend fun getCurrentUser(): Result<UserDto> {
        return safeApiCall { api.getCurrentUser() }
    }

    suspend fun updateAccount(request: UpdateAccountRequestDto): Result<UserDto> {
        return safeApiCall { api.updateAccount(request) }
    }

    suspend fun changePassword(request: ChangePasswordRequestDto): Result<Unit> {
        return safeApiCall { api.changePassword(request) }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return changePassword(
            ChangePasswordRequestDto(
                currentPassword = currentPassword,
                newPassword = newPassword
            )
        )
    }
}

