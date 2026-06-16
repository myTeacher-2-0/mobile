package com.crw.myteacher.data.remote

import com.crw.myteacher.data.remote.dto.AuthResponseDto
import com.crw.myteacher.data.remote.dto.ChatMessageResponse
import com.crw.myteacher.data.remote.dto.ChatRoomDto
import com.crw.myteacher.data.remote.dto.LoginRequestDto
import com.crw.myteacher.data.remote.dto.MeetingDto
import com.crw.myteacher.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MyTeacherApi {

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequestDto,
        @Header("X-Recaptcha-Token") recaptchaToken: String,
        @Header("X-Client-Platform") clientPlatform: String = "android"
    ): Response<AuthResponseDto>

    @GET("api/accounts/me")
    suspend fun getCurrentUser(): Response<UserDto>

    @GET("api/accounts/{id}")
    suspend fun getUserById(@Path("id") userId: String): Response<UserDto>

    @GET("api/meetings/me")
    suspend fun getMyMeetings(): Response<List<MeetingDto>>

    @GET("api/chat-rooms/me")
    suspend fun getMyChatRooms(
        @Query("before") before: String? = null,
        @Query("limit") after: Int? = null,
    ): Response<List<ChatRoomDto>>

    @GET("api/chat-messages/chat-room/{id}")
    suspend fun getLatestMessagesInChatRoom(
        @Path("id") chatRoomId: String,
        @Query("before") before: String? = null,
        @Query("limit") limit: Int = 20
    ): Response<List<ChatMessageResponse>>
}
