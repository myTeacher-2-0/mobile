package com.crw.myteacher.data.remote

import android.content.Context
import com.crw.myteacher.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object ApiClient {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private lateinit var tokenManager: TokenManager
    private lateinit var retrofit: Retrofit

    fun init(context: Context) {
        tokenManager = TokenManager(context.applicationContext)

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            )
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .callFactory(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    val api: MyTeacherApi by lazy {
        retrofit.create(MyTeacherApi::class.java)
    }

    fun getTokenManager(): TokenManager = tokenManager
}
