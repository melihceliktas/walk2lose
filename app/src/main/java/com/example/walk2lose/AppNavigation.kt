package com.example.walk2lose

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController)
        }
        composable("registration") {
            RegistrationScreen(navController)
        }
        composable("main") {
            MainScreen(
                navController = navController,
                currentWeight = 120,
                targetWeight = 95,
                dailyCalories = 1800,
                daysLeft = 30
            )
        }
        composable("challenge") {
            ChallengeScreen()
        }
        composable("profile") {
            ProfileScreen(navController)
        }
        composable("edit_profile") {
            EditProfileScreen(navController)
        }

    }
}