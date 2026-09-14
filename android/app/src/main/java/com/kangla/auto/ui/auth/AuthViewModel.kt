package com.kangla.auto.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kangla.auto.data.Repository
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val finished: Boolean = false,
)

class AuthViewModel : ViewModel() {

    var state by mutableStateOf(AuthUiState())
        private set

    fun login(phone: String, password: String) {
        if (state.loading) return
        viewModelScope.launch {
            state = AuthUiState(loading = true)
            try {
                Repository.login(phone.trim(), password)
                state = AuthUiState(finished = true)
            } catch (t: Throwable) {
                state = AuthUiState(error = Repository.errorMessage(t))
            }
        }
    }

    fun register(name: String, phone: String, password: String, role: String) {
        if (state.loading) return
        viewModelScope.launch {
            state = AuthUiState(loading = true)
            try {
                Repository.register(name.trim(), phone.trim(), password, role)
                state = AuthUiState(finished = true)
            } catch (t: Throwable) {
                state = AuthUiState(error = Repository.errorMessage(t))
            }
        }
    }
}