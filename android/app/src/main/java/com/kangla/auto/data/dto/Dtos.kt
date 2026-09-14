package com.kangla.auto.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Long,
    val name: String,
    val phone: String,
    val role: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserDto,
)

@Serializable
data class LoginRequest(
    val phone: String,
    val password: String,
)

@Serializable
data class RegisterRequest(
    val name: String,
    val phone: String,
    val password: String,
    val role: String,
)

@Serializable
data class BookingDto(
    val id: Long,
    @SerialName("user_id") val userId: Long,
    @SerialName("user_name") val userName: String,
    @SerialName("driver_id") val driverId: Long? = null,
    @SerialName("driver_name") val driverName: String? = null,
    val pickup: String,
    val dropoff: String,
    @SerialName("distance_km") val distanceKm: Double? = null,
    val fare: Double,
    val status: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class BookingRequest(
    val pickup: String,
    val dropoff: String,
    @SerialName("distance_km") val distanceKm: Double? = null,
)

@Serializable
data class StatusRequest(
    val status: String,
)