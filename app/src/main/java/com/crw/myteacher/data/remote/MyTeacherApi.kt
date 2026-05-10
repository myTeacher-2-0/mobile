package com.crw.myteacher.data.remote

import com.crw.myteacher.data.remote.dto.AuthResponseDto
import com.crw.myteacher.data.remote.dto.AvailabilityOverrideDto
import com.crw.myteacher.data.remote.dto.AvailabilityWeekDto
import com.crw.myteacher.data.remote.dto.ChangePasswordRequestDto
import com.crw.myteacher.data.remote.dto.CreateAvailabilityOverrideRequestDto
import com.crw.myteacher.data.remote.dto.CreateAvailabilityWeekRequestDto
import com.crw.myteacher.data.remote.dto.LoginRequestDto
import com.crw.myteacher.data.remote.dto.MeetingDto
import com.crw.myteacher.data.remote.dto.MeetingListResponseDto
import com.crw.myteacher.data.remote.dto.OfferDto
import com.crw.myteacher.data.remote.dto.OfferListResponseDto
import com.crw.myteacher.data.remote.dto.RegisterRequestDto
import com.crw.myteacher.data.remote.dto.UpdateAccountRequestDto
import com.crw.myteacher.data.remote.dto.UpdateAvailabilityOverrideRequestDto
import com.crw.myteacher.data.remote.dto.UpdateAvailabilityWeekRequestDto
import com.crw.myteacher.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface MyTeacherApi {

    // ── Auth ──────────────────────────────────────────────────

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body refreshToken: String): Response<AuthResponseDto>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    // ── Account Management ───────────────────────────────────

    @GET("api/accounts/me")
    suspend fun getCurrentUser(): Response<UserDto>

    @PUT("api/accounts/me")
    suspend fun updateAccount(@Body request: UpdateAccountRequestDto): Response<UserDto>

    @PATCH("api/accounts/me/password")
    suspend fun changePassword(@Body request: ChangePasswordRequestDto): Response<Unit>

    // ── Meetings ─────────────────────────────────────────────

    @GET("api/meetings")
    suspend fun getMeetings(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("status") status: String? = null
    ): Response<MeetingListResponseDto>

    @GET("api/meetings/{id}")
    suspend fun getMeetingById(@Path("id") meetingId: Long): Response<MeetingDto>

    // ── Availability Week ────────────────────────────────────

    @POST("api/availability/week")
    suspend fun createAvailabilityWeek(
        @Body request: CreateAvailabilityWeekRequestDto
    ): Response<AvailabilityWeekDto>

    @PATCH("api/availability/week")
    suspend fun updateAvailabilityWeek(
        @Body request: UpdateAvailabilityWeekRequestDto
    ): Response<AvailabilityWeekDto>

    @DELETE("api/availability/week")
    suspend fun deleteAvailabilityWeek(
        @Query("weekType") weekType: String
    ): Response<Unit>

    @GET("api/availability/week/me")
    suspend fun getMyAvailabilityWeeks(): Response<List<AvailabilityWeekDto>>

    @GET("api/availability/week/owner/{ownerId}")
    suspend fun getAvailabilityWeekByOwner(
        @Path("ownerId") ownerId: Long,
        @Query("weekType") weekType: String? = null
    ): Response<List<AvailabilityWeekDto>>

    // ── Availability Override ────────────────────────────────

    @POST("api/availability/override")
    suspend fun createAvailabilityOverride(
        @Body request: CreateAvailabilityOverrideRequestDto
    ): Response<AvailabilityOverrideDto>

    @PATCH("api/availability/override")
    suspend fun updateAvailabilityOverride(
        @Body request: UpdateAvailabilityOverrideRequestDto
    ): Response<AvailabilityOverrideDto>

    @DELETE("api/availability/override")
    suspend fun deleteAvailabilityOverride(
        @Query("date") date: String
    ): Response<Unit>

    @GET("api/availability/override/me")
    suspend fun getMyAvailabilityOverrides(): Response<List<AvailabilityOverrideDto>>

    @GET("api/availability/override/owner/{ownerId}")
    suspend fun getAvailabilityOverrideByOwner(
        @Path("ownerId") ownerId: Long
    ): Response<List<AvailabilityOverrideDto>>

    // ── Offers ───────────────────────────────────────────────

    @GET("api/offers")
    suspend fun getOffers(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<OfferListResponseDto>

    @GET("api/offers/{id}")
    suspend fun getOfferById(@Path("id") offerId: Long): Response<OfferDto>

    // ── Reservations ─────────────────────────────────────────

    @POST("api/reservations")
    suspend fun createReservation(@Body request: Map<String, @JvmSuppressWildcards Any>): Response<Unit>
}
