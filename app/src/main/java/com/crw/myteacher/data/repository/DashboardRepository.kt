package com.crw.myteacher.data.repository

import com.crw.myteacher.data.model.DashboardData
import com.crw.myteacher.data.remote.dto.UserDto

interface DashboardRepository {
    suspend fun getDashboard(): DashboardData
    suspend fun getDashboardForUser(user: UserDto): DashboardData
}

