@file:OptIn(ExperimentalMaterial3Api::class)

package com.kangla.auto.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kangla.auto.data.dto.BookingDto
import com.kangla.auto.ui.theme.Amber
import com.kangla.auto.ui.theme.AmberSoft
import com.kangla.auto.ui.theme.Danger
import com.kangla.auto.ui.theme.MaroonDark
import com.kangla.auto.ui.theme.Muted
import com.kangla.auto.ui.theme.Success

@Composable
fun BrandHeader(tagline: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .background(Amber, RoundedCornerShape(12.dp))
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Text("Kangla Auto", color = MaroonDark, fontWeight = FontWeight.Black, fontSize = 22.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(tagline, color = Muted, fontSize = 14.sp)
    }
}

@Composable
fun ErrorBanner(message: String?, modifier: Modifier = Modifier) {
    if (message == null) return
    Text(
        text = message,
        color = Danger,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier.padding(top = 4.dp),
    )
}

@Composable
fun InfoBanner(message: String?, modifier: Modifier = Modifier) {
    if (message == null) return
    Text(
        text = message,
        color = Success,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(top = 4.dp),
    )
}

@Composable
fun StatusBadge(status: String) {
    val (bg, fg) = when (status) {
        "pending" -> Color(0xFFFDEECA) to Color(0xFF92600A)
        "accepted" -> Color(0xFFD7ECFB) to Color(0xFF1F5F8B)
        "completed" -> Color(0xFFD9EFE2) to Color(0xFF1E6B4A)
        "cancelled" -> Color(0xFFF5DCDC) to Color(0xFF933434)
        else -> AmberSoft to MaroonDark
    }
    Text(
        text = status.replaceFirstChar { it.uppercase() },
        color = fg,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        modifier = Modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 3.dp),
    )
}

@Composable
fun BookingCard(
    booking: BookingDto,
    subtitle: String,
    actionLabel: String? = null,
    actionBusy: Boolean = false,
    onAction: (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "#${booking.id} · ${booking.pickup} → ${booking.dropoff}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                StatusBadge(booking.status)
            }
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Muted)
            Text(
                text = "₹${booking.fare.toInt()} · Driver: ${booking.driverName ?: "Not assigned"}",
                style = MaterialTheme.typography.bodyMedium,
            )
            if (actionLabel != null) {
                Spacer(Modifier.height(2.dp))
                Button(
                    onClick = { onAction?.invoke() },
                    enabled = !actionBusy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}