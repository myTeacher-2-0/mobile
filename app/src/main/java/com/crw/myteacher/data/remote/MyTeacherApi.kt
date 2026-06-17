package com.crw.myteacher.data.remote

import com.crw.myteacher.data.remote.dto.AuthResponseDto
import com.crw.myteacher.data.remote.dto.AvailabilityOverrideDto
import com.crw.myteacher.data.remote.dto.AvailabilityWeekDto
import com.crw.myteacher.data.remote.dto.ChatMessageResponse
import com.crw.myteacher.data.remote.dto.ChatRoomDto
import com.crw.myteacher.data.remote.dto.LoginRequestDto
import com.crw.myteacher.data.remote.dto.MeetingDto
import com.crw.myteacher.data.remote.dto.OfferDto
import com.crw.myteacher.data.remote.dto.OfferListResponseDto
import com.crw.myteacher.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
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

    @PUT("api/meetings/{id}/confirm")
    suspend fun confirmMeeting(@Path("id") meetingId: Long): Response<MeetingDto>

    @PUT("api/meetings/{id}/cancel")
    suspend fun cancelMeeting(@Path("id") meetingId: Long): Response<MeetingDto>

    @GET("api/meetings/{id}")
    suspend fun getMeetingById(@Path("id") meetingId: Long): Response<MeetingDto>

    @GET("api/availability/week/me")
    suspend fun getMyAvailabilityWeeks(): Response<List<AvailabilityWeekDto>>

    @GET("api/availability/week/owner/{ownerId}")
    suspend fun getAvailabilityWeekByOwner(
        @Path("ownerId") ownerId: Long,
        @Query("weekType") weekType: String? = null
    ): Response<List<AvailabilityWeekDto>>

    @GET("api/availability/override/me")
    suspend fun getMyAvailabilityOverrides(): Response<List<AvailabilityOverrideDto>>

    @GET("api/availability/override/owner/{ownerId}")
    suspend fun getAvailabilityOverrideByOwner(
        @Path("ownerId") ownerId: Long
    ): Response<List<AvailabilityOverrideDto>>

    @GET("api/offers")
    suspend fun getOffers(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<OfferListResponseDto>

    @GET("api/offers/{id}")
    suspend fun getOfferById(@Path("id") offerId: Long): Response<OfferDto>

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
