package com.example.walk2lose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


@Composable
fun IncompleteScreen(steps: Int, calories: Int, duration: String, navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,

    ) {
        Text("Bir Sonraki Sefere 💪!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Tahmini Atılan Adım: $steps")
        Text("Tahmini Yakılan Kalori: $calories kcal")
        Text("Geçen Süre: $duration")
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { navController.navigate("main"){
            popUpTo("incomplete/$steps/$calories/$duration") {inclusive = true}} }) {
            Text("Ana Ekrana Dön")
        }
    }
}

