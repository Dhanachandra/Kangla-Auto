package com.kangla.auto.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kangla.auto.data.Repository
import com.kangla.auto.data.dto.BookingDto
import kotlinx.coroutines.launch

data class DriverUiState(
    val busyId: Long? = null,
    val error: String? = null,
    val pending: List<BookingDto> = emptyList(),
    val mine: List<BookingDto> = emptyList(),
)

class DriverViewModel : ViewModel() {

    var state by mutableStateOf(DriverUiState())
        private set

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            try {
                state = state.copy(pending = Repository.pendingBookings(), mine = Repository.myBookings())
            } catch (t: Throwable) {
                state = state.copy(error = Repository.errorMessage(t))
            }
        }
    }

    fun acceptBooking(bookingId: Long) {
        transition(bookingId) { Repository.updateBooking(bookingId, "accepted") }
    }

    fun completeBooking(bookingId: Long) {
        transition(bookingId) { Repository.updateBooking(bookingId, "completed") }
    }

    private fun transition(bookingId: Long, action: suspend () -> Unit) {
        viewModelScope.launch {
            state = state.copy(busyId = bookingId, error = null)
            try {
                action()
                refreshAll()
            } catch (t: Throwable) {
                state = state.copy(busyId = null, error = Repository.errorMessage(t))
            }
        }
    }
}