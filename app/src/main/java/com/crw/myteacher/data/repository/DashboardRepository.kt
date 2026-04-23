package com.crw.myteacher.data.repository

import com.crw.myteacher.data.model.DashboardData

interface DashboardRepository {
    suspend fun getDashboard(): DashboardData
}

