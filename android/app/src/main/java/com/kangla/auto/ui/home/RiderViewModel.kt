package com.kangla.auto.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kangla.auto.data.Repository
import com.kangla.auto.data.dto.BookingDto
import kotlinx.coroutines.launch

data class RiderUiState(
    val creating: Boolean = false,
    val error: String? = null,
    val info: String? = null,
    val rides: List<BookingDto> = emptyList(),
)

class RiderViewModel : ViewModel() {

    var state by mutableStateOf(RiderUiState())
        private set

    init {
        refreshRides()
    }

    fun refreshRides() {
        viewModelScope.launch {
            try {
                state = state.copy(rides = Repository.myBookings())
            } catch (t: Throwable) {
                state = state.copy(error = Repository.errorMessage(t))
            }
        }
    }

    fun createBooking(pickup: String, dropoff: String, distanceKm: Double?) {
        if (state.creating) return
        viewModelScope.launch {
            state = state.copy(creating = true, error = null, info = null)
            try {
                Repository.createBooking(pickup.trim(), dropoff.trim(), distanceKm)
                state = state.copy(creating = false, info = "Booking placed ✓")
                refreshRides()
            } catch (t: Throwable) {
                state = state.copy(creating = false, error = Repository.errorMessage(t))
            }
        }
    }

    fun cancelBooking(bookingId: Long) {
        viewModelScope.launch {
            try {
                Repository.updateBooking(bookingId, "cancelled")
                refreshRides()
            } catch (t: Throwable) {
                state = state.copy(error = Repository.errorMessage(t))
            }
        }
    }

    fun clearInfo() {
        state = state.copy(info = null)
    }
}