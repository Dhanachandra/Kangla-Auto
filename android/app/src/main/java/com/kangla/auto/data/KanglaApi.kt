package com.kangla.auto.data

import com.kangla.auto.data.dto.AuthResponse
import com.kangla.auto.data.dto.BookingDto
import com.kangla.auto.data.dto.BookingRequest
import com.kangla.auto.data.dto.LoginRequest
import com.kangla.auto.data.dto.RegisterRequest
import com.kangla.auto.data.dto.StatusRequest
import com.kangla.auto.data.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface KanglaApi {

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @GET("api/auth/me")
    suspend fun me(): UserDto

    @GET("api/drivers")
    suspend fun drivers(): List<UserDto>

    @POST("api/bookings")
    suspend fun createBooking(@Body body: BookingRequest): BookingDto

    @GET("api/bookings/my")
    suspend fun myBookings(): List<BookingDto>

    @GET("api/bookings/pending")
    suspend fun pendingBookings(): List<BookingDto>

    @PATCH("api/bookings/{id}")
    suspend fun updateBooking(@Path("id") id: Long, @Body body: StatusRequest): BookingDto
}