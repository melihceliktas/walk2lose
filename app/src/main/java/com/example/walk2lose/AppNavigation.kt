package com.example.walk2lose

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument


@RequiresApi(Build.VERSION_CODES.O)
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




            )
        }
        composable("challenge/{steps}", arguments = listOf(
            navArgument("steps") { type = NavType.IntType }
        )) { backStackEntry ->
            val steps = backStackEntry.arguments?.getInt("steps") ?: 3000
            ChallengeScreen(selectedSteps = steps, navController = navController)
        }


        //geriye dönmeyi  engellemeyi unutma

        composable("finish/{steps}/{calories}/{duration}",listOf(
            navArgument("steps") { type = NavType.IntType },
            navArgument("calories") { type = NavType.IntType },
            navArgument("duration") {type = NavType.StringType}
        )) { backStackEntry ->

            val steps = backStackEntry.arguments?.getInt("steps") ?: 0
            val calories = backStackEntry.arguments?.getInt("calories") ?: 0
            val duration = backStackEntry.arguments?.getString("duration") ?: "00:00"

            FinishScreen(steps = steps, calories = calories,navController = navController, duration = duration)
        }

        composable("incomplete/{steps}/{calories}/{duration}", listOf(
            navArgument("steps") {type = NavType.IntType},
            navArgument("calories") { type = NavType.IntType},
            navArgument("duration") {type = NavType.StringType}
        )) {backStackEntry ->

            val steps = backStackEntry.arguments?.getInt("steps") ?: 0
            val calories = backStackEntry.arguments?.getInt("calories") ?: 0
            val duration = backStackEntry.arguments?.getString("duration") ?: "00:00"

            IncompleteScreen(steps = steps, calories = calories, duration = duration, navController = navController)

        }

        composable("profile") {
            ProfileScreen(navController)
        }
        composable("edit_profile") {
            EditProfileScreen(navController)
        }



    }
}