package com.example.walk2lose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument


@Composable
fun AppNavigation(

) {
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


                dailyCalories = 1800

            )
        }
        composable("challenge/{steps}", arguments = listOf(
            navArgument("steps") { type = NavType.IntType }
        )) { backStackEntry ->
            val steps = backStackEntry.arguments?.getInt("steps") ?: 3000
            ChallengeScreen(selectedSteps = steps)
        }

        composable("profile") {
            ProfileScreen(navController)
        }
        composable("edit_profile") {
            EditProfileScreen(navController)
        }



    }
}