package com.kangla.auto.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kangla.auto.data.dto.BookingDto
import com.kangla.auto.data.dto.UserDto
import com.kangla.auto.ui.components.BookingCard
import com.kangla.auto.ui.components.ErrorBanner
import com.kangla.auto.ui.theme.Muted

private enum class DriverTab(val label: String) {
    Open("Open bookings"),
    Mine("My rides"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreen(
    user: UserDto,
    onLoggedOut: () -> Unit,
    viewModel: DriverViewModel = viewModel(),
) {
    var tab by remember { mutableStateOf(DriverTab.Open) }
    val state = viewModel.state

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kangla Auto — Drivers") },
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
                .padding(16.dp),
        ) {
            Text("Hi ${user.name} · ${user.phone}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Accept open bookings, complete rides, and earn.", color = Muted, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            ErrorBanner(state.error)

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DriverTab.entries.forEach { t ->
                    Button(
                        onClick = { tab = t },
                        enabled = tab != t,
                    ) {
                        Text(t.label + countSuffix(state, t))
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Box(Modifier.weight(1f)) {
                when (tab) {
                    DriverTab.Open -> BookingList(
                        bookings = state.pending,
                        emptyMessage = "No open bookings right now.",
                        actionLabel = "Accept",
                        busyId = state.busyId,
                        onAction = viewModel::acceptBooking,
                    )
                    DriverTab.Mine -> BookingList(
                        bookings = state.mine,
                        emptyMessage = "You have no accepted rides yet.",
                        actionLabel = "Complete ride",
                        busyId = state.busyId,
                        onAction = viewModel::completeBooking,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Button(onClick = onLoggedOut, modifier = Modifier.fillMaxWidth()) {
                Text("Log out")
            }
        }
    }
}

private fun countSuffix(state: DriverUiState, tab: DriverTab): String {
    val count = if (tab == DriverTab.Open) state.pending.size else state.mine.size
    return if (count > 0) " ($count)" else ""
}

@Composable
private fun BookingList(
    bookings: List<BookingDto>,
    emptyMessage: String,
    actionLabel: String,
    busyId: Long?,
    onAction: (Long) -> Unit,
) {
    if (bookings.isEmpty()) {
        Text(
            if (busyId != null) "Refreshing…" else emptyMessage,
            color = Muted,
            modifier = Modifier.padding(top = 8.dp),
        )
        return
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier,
    ) {
        items(bookings, key = { it.id }) { booking ->
            val canAct = when (booking.status) {
                "pending" -> actionLabel == "Accept" && busyId != booking.id
                "accepted" -> actionLabel == "Complete ride" && busyId != booking.id
                else -> false
            }
            BookingCard(
                booking = booking,
                subtitle = "Rider: ${booking.userName} · Placed ${booking.createdAt.take(10)}",
                actionLabel = if (canAct || (busyId == booking.id)) actionLabel else null,
                actionBusy = busyId == booking.id,
                onAction = if (canAct) { { onAction(booking.id) } } else null,
            )
        }
    }
}