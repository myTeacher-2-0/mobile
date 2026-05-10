package com.crw.myteacher.data.repository

import com.crw.myteacher.data.remote.MyTeacherApi
import com.crw.myteacher.data.remote.dto.MeetingDto
import com.crw.myteacher.data.remote.dto.MeetingListResponseDto

class MeetingRepository(private val api: MyTeacherApi) : BaseRepository() {

    suspend fun getMeetings(
        page: Int = 0,
        size: Int = 20,
        status: String? = null
    ): Result<MeetingListResponseDto> {
        return safeApiCall { api.getMeetings(page = page, size = size, status = status) }
    }

    suspend fun getMeetingsList(
        page: Int = 0,
        size: Int = 20,
        status: String? = null
    ): Result<List<MeetingDto>> {
        return getMeetings(page, size, status).map { it.content }
    }

    suspend fun getMeetingById(meetingId: Long): Result<MeetingDto> {
        return safeApiCall { api.getMeetingById(meetingId) }
    }
}
