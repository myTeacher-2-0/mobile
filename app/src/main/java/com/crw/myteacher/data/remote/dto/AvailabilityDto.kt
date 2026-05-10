package com.crw.myteacher.data.remote.dto

import kotlinx.serialization.Serializable

// ── Availability Week ────────────────────────────────────────

@Serializable
data class AvailabilityWeekDto(
    val id: Long? = null,
    val ownerId: Long? = null,
    val weekType: String, // e.g. "DEFAULT", "ODD", "EVEN"
    val days: List<AvailabilityDayDto> = emptyList()
)

@Serializable
data class AvailabilityDayDto(
    val dayOfWeek: String, // "MONDAY", "TUESDAY", etc.
    val slots: List<AvailabilitySlotDto> = emptyList()
)

@Serializable
data class AvailabilitySlotDto(
    val startTime: String, // "HH:mm"
    val endTime: String    // "HH:mm"
)

@Serializable
data class CreateAvailabilityWeekRequestDto(
    val weekType: String,
    val days: List<AvailabilityDayDto> = emptyList()
)

@Serializable
data class UpdateAvailabilityWeekRequestDto(
    val weekType: String,
    val days: List<AvailabilityDayDto> = emptyList()
)

// ── Availability Override ────────────────────────────────────

@Serializable
data class AvailabilityOverrideDto(
    val id: Long? = null,
    val ownerId: Long? = null,
    val date: String, // "yyyy-MM-dd"
    val overrideType: String? = null, // e.g. "AVAILABLE", "UNAVAILABLE"
    val slots: List<AvailabilitySlotDto> = emptyList()
)

@Serializable
data class CreateAvailabilityOverrideRequestDto(
    val date: String,
    val overrideType: String? = null,
    val slots: List<AvailabilitySlotDto> = emptyList()
)

@Serializable
data class UpdateAvailabilityOverrideRequestDto(
    val date: String,
    val overrideType: String? = null,
    val slots: List<AvailabilitySlotDto> = emptyList()
)

// ── Combined response ────────────────────────────────────────

@Serializable
data class AvailabilityResponseDto(
    val weeks: List<AvailabilityWeekDto> = emptyList(),
    val overrides: List<AvailabilityOverrideDto> = emptyList()
)

