package com.kangla.auto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kangla.auto.data.Repository
import com.kangla.auto.ui.auth.LoginScreen
import com.kangla.auto.ui.auth.RegisterScreen
import com.kangla.auto.ui.home.HomeRouter
import com.kangla.auto.ui.theme.KanglaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KanglaTheme {
                val session by Repository.session.collectAsState()
                val startDestination = if (session != null) "home" else "login"
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("login") {
                        LoginScreen(
                            onLoggedIn = { navController.navigate("home") { popUpTo(0) { inclusive = true } } },
                            navigateToRegister = { navController.navigate("register") },
                        )
                    }
                    composable("register") {
                        RegisterScreen(
                            onLoggedIn = { navController.navigate("home") { popUpTo(0) { inclusive = true } } },
                            navigateToLogin = { navController.popBackStack() },
                        )
                    }
                    composable("home") {
                        HomeRouter(onLoggedOut = {
                            navController.navigate("login") { popUpTo(0) { inclusive = true } }
                        })
                    }
                }
            }
        }
    }
}