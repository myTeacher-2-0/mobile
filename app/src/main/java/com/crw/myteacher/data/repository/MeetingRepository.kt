package com.crw.myteacher.data.repository

import com.crw.myteacher.data.remote.MyTeacherApi
import com.crw.myteacher.data.remote.dto.MeetingDto

class MeetingRepository(private val api: MyTeacherApi) : BaseRepository() {

    suspend fun getMyMeetings(): Result<List<MeetingDto>> {
        return safeApiCall { api.getMyMeetings() }
    }
}
