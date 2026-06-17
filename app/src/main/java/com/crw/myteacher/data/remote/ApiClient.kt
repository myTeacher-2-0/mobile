package com.crw.myteacher.data.remote

import android.content.Context
import android.util.Log
import com.crw.myteacher.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object ApiClient {
    private const val TAG = "ApiClient"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private lateinit var tokenManager: TokenManager
    private lateinit var retrofit: Retrofit
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) {
            Log.d(TAG, "init() SKIPPED — already initialized")
            return
        }

        Log.d(TAG, "init() — initializing ApiClient")
        tokenManager = TokenManager(context.applicationContext)

        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("OkHttp", message)
        }.apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val req = chain.request()
                val currentToken = tokenManager.accessToken
                Log.d(TAG, "┌─ PRE-AUTH CHECK ─ ${req.method} ${req.url.encodedPath}")
                Log.d(TAG, "│  Token in storage: ${if (currentToken.isNullOrBlank()) "EMPTY/NULL" else currentToken.take(20) + "..."}")
                Log.d(TAG, "│  Token length: ${currentToken?.length ?: 0}")
                chain.proceed(req)
            }
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(loggingInterceptor)
            .addNetworkInterceptor { chain ->
                val req = chain.request()
                Log.d(TAG, "┌─ NETWORK ─ ${req.method} ${req.url}")
                Log.d(TAG, "│  Authorization header present: ${req.header("Authorization") != null}")
                val resp = chain.proceed(req)
                Log.d(TAG, "└─ NETWORK ─ HTTP ${resp.code} for ${req.url.encodedPath}")
                resp
            }
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .callFactory(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        isInitialized = true
        Log.d(TAG, "init() — DONE, baseUrl=${BuildConfig.API_BASE_URL}")
    }

    val api: MyTeacherApi by lazy {
        retrofit.create(MyTeacherApi::class.java)
    }

    val chatStompClient: ChatStompClient by lazy {
        ChatStompClient(tokenManager)
    }

    fun getTokenManager(): TokenManager = tokenManager
}
