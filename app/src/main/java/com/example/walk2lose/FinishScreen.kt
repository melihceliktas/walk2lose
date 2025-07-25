package com.example.walk2lose

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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController


@Composable

fun FinishScreen(
    navController: NavController,
    steps: Int,
    calories: Int
    ) {


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Tebrikler!",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Challenge verilerini göster
        Text(
            text = "Yaklaşık Yakılan Kalori: ${calories} kcal",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Yaklaşık Adım Sayısı: ${steps} ", // Buraya mesafe verisi eklenecek
            style = MaterialTheme.typography.bodyLarge
        )


        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {

            navController.navigate("main")

        }) {
            Text(text = "Ana Ekrana Dön")
        }
    }

}