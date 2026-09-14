package com.kangla.auto.data

import android.content.Context
import com.kangla.auto.data.dto.BookingDto
import com.kangla.auto.data.dto.BookingRequest
import com.kangla.auto.data.dto.LoginRequest
import com.kangla.auto.data.dto.RegisterRequest
import com.kangla.auto.data.dto.StatusRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object Repository {

    private lateinit var tokenStore: TokenStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    private val _session = MutableStateFlow<Session?>(null)
    val session: StateFlow<Session?> = _session.asStateFlow()

    fun init(context: Context) {
        tokenStore = TokenStore(context.applicationContext)
        scope.launch { _session.value = tokenStore.session.first() }
    }

    fun currentToken(): String? = _session.value?.token

    suspend fun login(phone: String, password: String) {
        val res = ApiClient.api.login(LoginRequest(phone, password))
        tokenStore.save(res.token, res.user)
        _session.value = Session(res.token, res.user)
    }

    suspend fun register(name: String, phone: String, password: String, role: String) {
        val res = ApiClient.api.register(RegisterRequest(name, phone, password, role))
        tokenStore.save(res.token, res.user)
        _session.value = Session(res.token, res.user)
    }

    suspend fun logout() {
        tokenStore.clear()
        _session.value = null
    }

    suspend fun myBookings(): List<BookingDto> = ApiClient.api.myBookings()

    suspend fun pendingBookings(): List<BookingDto> = ApiClient.api.pendingBookings()

    suspend fun createBooking(pickup: String, dropoff: String, distanceKm: Double?) =
        ApiClient.api.createBooking(BookingRequest(pickup, dropoff, distanceKm))

    suspend fun updateBooking(bookingId: Long, status: String) =
        ApiClient.api.updateBooking(bookingId, StatusRequest(status))

    fun errorMessage(t: Throwable): String = when (t) {
        is HttpException -> {
            val detail = runCatching {
                val body = t.response()?.errorBody()?.string().orEmpty()
                json.parseToJsonElement(body).jsonObject["detail"]?.jsonPrimitive?.content
            }.getOrNull()
            detail?.let { "Server: $it" } ?: "Server error (${t.code()})"
        }
        is UnknownHostException -> "Cannot reach the server. Check your connection and API_BASE_URL."
        is SocketTimeoutException -> "Server took too long to respond."
        else -> t.message ?: "Something went wrong."
    }
}