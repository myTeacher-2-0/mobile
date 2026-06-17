package com.crw.myteacher.data.repository

import com.crw.myteacher.data.remote.MyTeacherApi
import com.crw.myteacher.data.remote.dto.OfferDto

class OfferRepository(private val api: MyTeacherApi) : BaseRepository() {

    suspend fun getOfferById(offerId: String): Result<OfferDto> {
        return safeApiCall { api.getOfferById(offerId) }
    }
}

