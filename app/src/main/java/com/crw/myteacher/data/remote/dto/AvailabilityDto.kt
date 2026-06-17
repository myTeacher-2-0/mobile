package com.crw.myteacher.data.remote.dto

import kotlinx.serialization.Serializable


@Serializable
data class AvailabilityWeekDto(
    val id: Long? = null,
    val ownerId: Long? = null,
    val weekType: String,
    val days: List<AvailabilityDayDto> = emptyList()
)

@Serializable
data class AvailabilityDayDto(
    val dayOfWeek: String,
    val slots: List<AvailabilitySlotDto> = emptyList()
)

@Serializable
data class AvailabilitySlotDto(
    val startTime: String,
    val endTime: String
)

@Serializable
data class AvailabilityOverrideDto(
    val id: Long? = null,
    val ownerId: Long? = null,
    val date: String,
    val overrideType: String? = null,
    val slots: List<AvailabilitySlotDto> = emptyList()
)
