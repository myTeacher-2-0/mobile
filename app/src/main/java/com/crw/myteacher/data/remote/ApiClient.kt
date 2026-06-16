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
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return

        tokenManager = TokenManager(context.applicationContext)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(loggingInterceptor)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .callFactory(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        isInitialized = true
    }

    val api: MyTeacherApi by lazy {
        retrofit.create(MyTeacherApi::class.java)
    }

    val chatStompClient: ChatStompClient by lazy {
        ChatStompClient(tokenManager)
    }

    fun getTokenManager(): TokenManager = tokenManager
}
