package com.crw.myteacher.data.repository

import com.crw.myteacher.data.remote.MyTeacherApi
import com.crw.myteacher.data.remote.dto.MeetingDto
import com.crw.myteacher.data.remote.dto.MeetingListResponseDto

class MeetingRepository(private val api: MyTeacherApi) : BaseRepository() {

    suspend fun getMyMeetings(): Result<MeetingListResponseDto> {
        return safeApiCall { api.getMyMeetings() }
    }

    suspend fun confirmMeeting(meetingId: Long): Result<MeetingDto> {
        return safeApiCall { api.confirmMeeting(meetingId) }
    }

    suspend fun cancelMeeting(meetingId: Long): Result<MeetingDto> {
        return safeApiCall { api.cancelMeeting(meetingId) }
    }

    suspend fun getMeetingById(meetingId: Long): Result<MeetingDto> {
        return safeApiCall { api.getMeetingById(meetingId) }
    }

    suspend fun getFutureMeetingsByOwner(ownerId: Long): Result<MeetingListResponseDto> {
        return safeApiCall { api.getFutureMeetingsByOwner(ownerId) }
    }
}
