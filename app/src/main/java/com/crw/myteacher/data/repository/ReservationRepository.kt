package com.crw.myteacher.data.repository

import com.crw.myteacher.data.remote.MyTeacherApi

class ReservationRepository(private val api: MyTeacherApi) : BaseRepository() {

    suspend fun createReservation(
        request: Map<String, @JvmSuppressWildcards Any>
    ): Result<Unit> {
        return safeApiCall { api.createReservation(request) }
    }

    suspend fun createReservation(
        teacherId: Long,
        offerId: Long,
        startTime: String,
        endTime: String,
        notes: String? = null
    ): Result<Unit> {
        val body = buildMap<String, Any> {
            put("teacherId", teacherId)
            put("offerId", offerId)
            put("startTime", startTime)
            put("endTime", endTime)
            if (notes != null) put("notes", notes)
        }
        return createReservation(body)
    }
}

