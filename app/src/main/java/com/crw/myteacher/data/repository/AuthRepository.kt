package com.crw.myteacher.data.repository

import com.crw.myteacher.data.remote.MyTeacherApi
import com.crw.myteacher.data.remote.dto.AuthResponseDto
import com.crw.myteacher.data.remote.dto.LoginRequestDto
import com.crw.myteacher.data.remote.dto.RegisterRequestDto

class AuthRepository(private val api: MyTeacherApi) : BaseRepository() {

    suspend fun register(request: RegisterRequestDto): Result<AuthResponseDto> {
        return safeApiCall { api.register(request) }
    }

    suspend fun login(request: LoginRequestDto): Result<AuthResponseDto> {
        return safeApiCall { api.login(request) }
    }

    suspend fun login(email: String, password: String): Result<AuthResponseDto> {
        return login(LoginRequestDto(email = email, password = password))
    }

    suspend fun refreshToken(refreshToken: String): Result<AuthResponseDto> {
        return safeApiCall { api.refreshToken(refreshToken) }
    }

    suspend fun logout(): Result<Unit> {
        return safeApiCall { api.logout() }
    }
}

