package com.kangla.auto.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kangla.auto.ui.components.BrandHeader
import com.kangla.auto.ui.components.ErrorBanner
import com.kangla.auto.ui.theme.Muted

private val roles = listOf("rider" to "Rider (I book rides)", "driver" to "Driver (I pick riders)")

@Composable
fun RegisterScreen(
    onLoggedIn: () -> Unit,
    navigateToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel(),
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("rider") }
    val state = viewModel.state

    LaunchedEffect(state.finished) {
        if (state.finished) onLoggedIn()
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            BrandHeader("Join Kangla Auto")
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full name") },
                singleLine = true,
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone number") },
                singleLine = true,
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password (min 6 chars)") },
                singleLine = true,
                enabled = !state.loading,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )

            Column(Modifier.fillMaxWidth().selectableGroup().padding(top = 8.dp)) {
                roles.forEach { (value, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = role == value,
                                onClick = { role = value },
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = role == value,
                            onClick = { role = value },
                            enabled = !state.loading,
                        )
                        Text(label, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }

            ErrorBanner(state.error)

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.register(name, phone, password, role) },
                enabled = !state.loading && name.isNotBlank() && phone.length >= 6 && password.length >= 6,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.height(8.dp))
                }
                Text(if (state.loading) "Creating…" else "Create account")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Have an account?", color = Muted)
                TextButton(onClick = navigateToLogin, enabled = !state.loading) {
                    Text("Log in")
                }
            }
        }
    }
}