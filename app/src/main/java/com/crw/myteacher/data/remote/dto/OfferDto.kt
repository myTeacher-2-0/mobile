package com.crw.myteacher.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OfferDto(
    val id: Long,
    val teacherId: Long? = null,
    val teacherName: String? = null,
    val teacherDescription: String? = null,
    val teacherRating: Float? = null,
    val teacherLessonsCount: Int? = null,
    val subjectName: String? = null,
    val pricePerLesson: Double = 0.0,
    val durationMinutes: Int = 60,
    val currency: String = "PLN"
)

@Serializable
data class OfferListResponseDto(
    val content: List<OfferDto> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0
)

