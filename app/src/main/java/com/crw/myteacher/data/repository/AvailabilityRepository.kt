package com.crw.myteacher.data.repository

import com.crw.myteacher.data.remote.MyTeacherApi
import com.crw.myteacher.data.remote.dto.AvailabilityOverrideDto
import com.crw.myteacher.data.remote.dto.AvailabilityWeekDto

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

    // ── Override ─────────────────────────────────────────────

    suspend fun getMyAvailabilityOverrides(): Result<List<AvailabilityOverrideDto>> {
        return safeApiCall { api.getMyAvailabilityOverrides() }
    }

    suspend fun getAvailabilityOverrideByOwner(
        ownerId: Long
    ): Result<List<AvailabilityOverrideDto>> {
        return safeApiCall { api.getAvailabilityOverrideByOwner(ownerId) }
    }
}
