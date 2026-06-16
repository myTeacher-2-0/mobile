package com.crw.myteacher.data.repository

import com.crw.myteacher.data.remote.MyTeacherApi
import com.crw.myteacher.data.remote.dto.AuthResponseDto
import com.crw.myteacher.data.remote.dto.LoginRequestDto

class AuthRepository(private val api: MyTeacherApi) : BaseRepository() {

    suspend fun login(request: LoginRequestDto, recaptchaToken: String): Result<AuthResponseDto> {
        return safeApiCall { api.login(request, recaptchaToken) }
    }

    suspend fun login(email: String, password: String, recaptchaToken: String): Result<AuthResponseDto> {
        return login(LoginRequestDto(email = email, password = password), recaptchaToken)
    }
}
