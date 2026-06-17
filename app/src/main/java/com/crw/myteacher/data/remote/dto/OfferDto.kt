package com.crw.myteacher.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OfferDto(
    val offerId: String? = null,
    val ownerId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val priceValue: String? = null,
    val priceCurrency: String? = null,
    val subject: String? = null,
    val levels: List<String> = emptyList(),
    val experienceDateFrom: String? = null,
    val ratingScore: Float? = null
)

@Serializable
data class OfferListResponseDto(
    val content: List<OfferDto> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0
)
