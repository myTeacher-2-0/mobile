package com.crw.myteacher.data.repository

import com.crw.myteacher.data.remote.MyTeacherApi
import com.crw.myteacher.data.remote.dto.OfferDto
import com.crw.myteacher.data.remote.dto.OfferListResponseDto

class OfferRepository(private val api: MyTeacherApi) : BaseRepository() {

    suspend fun getOffers(
        page: Int = 0,
        size: Int = 20
    ): Result<OfferListResponseDto> {
        return safeApiCall { api.getOffers(page = page, size = size) }
    }

    suspend fun getOffersList(
        page: Int = 0,
        size: Int = 20
    ): Result<List<OfferDto>> {
        return getOffers(page, size).map { it.content }
    }

    suspend fun getOfferById(offerId: Long): Result<OfferDto> {
        return safeApiCall { api.getOfferById(offerId) }
    }
}

