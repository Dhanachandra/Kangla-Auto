package com.kangla.auto.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.kangla.auto.data.Repository

@Composable
fun HomeRouter(onLoggedOut: () -> Unit) {
    val session by Repository.session.collectAsState()
    val user = session?.user

    if (user == null) {
        LaunchedEffect(Unit) { onLoggedOut() }
        return
    }

    when (user.role) {
        "driver" -> DriverHomeScreen(user = user, onLoggedOut = onLoggedOut)
        else -> RiderHomeScreen(user = user, onLoggedOut = onLoggedOut)
    }
}