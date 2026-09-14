package com.kangla.auto.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kangla.auto.data.dto.UserDto
import com.kangla.auto.ui.components.BookingCard
import com.kangla.auto.ui.components.ErrorBanner
import com.kangla.auto.ui.components.InfoBanner
import com.kangla.auto.ui.theme.Muted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiderHomeScreen(
    user: UserDto,
    onLoggedOut: () -> Unit,
    viewModel: RiderViewModel = viewModel(),
) {
    var pickup by remember { mutableStateOf("") }
    var dropoff by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current

    val state = viewModel.state
    LaunchedEffect(state.info) {
        if (state.info != null) viewModel.clearInfo()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kangla Auto — Riders") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(16.dp),
        ) {
            Text("Hi ${user.name} · ${user.phone}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Book a ride below, then track it in My bookings.", color = Muted, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            ErrorBanner(state.error)
            InfoBanner(state.info)

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = pickup,
                onValueChange = { pickup = it },
                label = { Text("Pickup") },
                singleLine = true,
                enabled = !state.creating,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = dropoff,
                onValueChange = { dropoff = it },
                label = { Text("Dropoff") },
                singleLine = true,
                enabled = !state.creating,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = distance,
                onValueChange = { distance = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Distance in km (optional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                enabled = !state.creating,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {
                    keyboard?.hide()
                    viewModel.createBooking(pickup, dropoff, distance.toDoubleOrNull())
                },
                enabled = !state.creating && pickup.isNotBlank() && dropoff.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.creating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.height(8.dp))
                }
                Text(if (state.creating) "Placing booking…" else "Request ride")
            }

            Spacer(Modifier.height(18.dp))

            Text("My bookings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            Box(Modifier.weight(1f).padding(top = 8.dp)) {
                if (state.rides.isEmpty()) {
                    Text("No bookings yet.", color = Muted)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(state.rides, key = { it.id }) { booking ->
                            val canCancel = booking.status == "pending" || booking.status == "accepted"
                            BookingCard(
                                booking = booking,
                                subtitle = "Rider: ${booking.userName} · Placed ${booking.createdAt.take(10)}",
                                actionLabel = if (canCancel) "Cancel ride" else null,
                                onAction = if (canCancel) { { viewModel.cancelBooking(booking.id) } } else null,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Button(onClick = onLoggedOut, modifier = Modifier.fillMaxWidth()) {
                Text("Log out")
            }
        }
    }
}