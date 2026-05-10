package com.crw.myteacher.data.repository

import retrofit2.Response

abstract class BaseRepository {

    protected suspend fun <T> safeApiCall(
        call: suspend () -> Response<T>
    ): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    @Suppress("UNCHECKED_CAST")
                    Result.success(Unit as T)
                }
            } else {
                Result.failure(ApiException(response.code(), response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class ApiException(
    val code: Int,
    override val message: String
) : Exception("HTTP $code: $message")

