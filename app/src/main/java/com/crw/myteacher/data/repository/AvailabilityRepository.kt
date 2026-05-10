package com.crw.myteacher.data.repository

import com.crw.myteacher.data.remote.MyTeacherApi
import com.crw.myteacher.data.remote.dto.AvailabilityOverrideDto
import com.crw.myteacher.data.remote.dto.AvailabilityWeekDto
import com.crw.myteacher.data.remote.dto.CreateAvailabilityOverrideRequestDto
import com.crw.myteacher.data.remote.dto.CreateAvailabilityWeekRequestDto
import com.crw.myteacher.data.remote.dto.UpdateAvailabilityOverrideRequestDto
import com.crw.myteacher.data.remote.dto.UpdateAvailabilityWeekRequestDto

class AvailabilityRepository(private val api: MyTeacherApi) : BaseRepository() {

    // ── Week ─────────────────────────────────────────────────

    suspend fun getMyAvailabilityWeeks(): Result<List<AvailabilityWeekDto>> {
        return safeApiCall { api.getMyAvailabilityWeeks() }
    }

    suspend fun getAvailabilityWeekByOwner(
        ownerId: Long,
        weekType: String? = null
    ): Result<List<AvailabilityWeekDto>> {
        return safeApiCall { api.getAvailabilityWeekByOwner(ownerId, weekType) }
    }

    suspend fun createAvailabilityWeek(
        request: CreateAvailabilityWeekRequestDto
    ): Result<AvailabilityWeekDto> {
        return safeApiCall { api.createAvailabilityWeek(request) }
    }

    suspend fun updateAvailabilityWeek(
        request: UpdateAvailabilityWeekRequestDto
    ): Result<AvailabilityWeekDto> {
        return safeApiCall { api.updateAvailabilityWeek(request) }
    }

    suspend fun deleteAvailabilityWeek(weekType: String): Result<Unit> {
        return safeApiCall { api.deleteAvailabilityWeek(weekType) }
    }

    // ── Override ─────────────────────────────────────────────

    suspend fun getMyAvailabilityOverrides(): Result<List<AvailabilityOverrideDto>> {
        return safeApiCall { api.getMyAvailabilityOverrides() }
    }

    suspend fun getAvailabilityOverrideByOwner(
        ownerId: Long
    ): Result<List<AvailabilityOverrideDto>> {
        return safeApiCall { api.getAvailabilityOverrideByOwner(ownerId) }
    }

    suspend fun createAvailabilityOverride(
        request: CreateAvailabilityOverrideRequestDto
    ): Result<AvailabilityOverrideDto> {
        return safeApiCall { api.createAvailabilityOverride(request) }
    }

    suspend fun updateAvailabilityOverride(
        request: UpdateAvailabilityOverrideRequestDto
    ): Result<AvailabilityOverrideDto> {
        return safeApiCall { api.updateAvailabilityOverride(request) }
    }

    suspend fun deleteAvailabilityOverride(date: String): Result<Unit> {
        return safeApiCall { api.deleteAvailabilityOverride(date) }
    }
}
