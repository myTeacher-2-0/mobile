package com.crw.myteacher.data.remote

import com.crw.myteacher.data.remote.dto.DashboardDto
import retrofit2.http.GET

interface MyTeacherApi {
    @GET("api/mobile/dashboard")
    suspend fun getDashboard(): DashboardDto
}

