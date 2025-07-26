package com.example.walk2lose

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon

import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Scaffold

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = viewModel()
) {
    LaunchedEffect(Unit) { viewModel.loadUserData() }

    val userData by viewModel.userData.collectAsState()
    val dailyCalories = userData?.caloriesBurned ?: 0

    Scaffold(
        modifier = Modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    MaterialTheme.colorScheme.background
                )
            )
        ),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("profile") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Person, contentDescription = "Profili Düzenle")
            }
        }
    ) { innerPadding ->

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.background
                        )
                    ))
        ) {

            val screenHeight = maxHeight
            val topSpacer = screenHeight * 0.1f
            val betweenHeaderAndStats = screenHeight * 0.05f
            val betweenStatsAndButtons = screenHeight * 0.1f
            val betweenButtons = screenHeight * 0.04f

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(topSpacer))

                Text(
                    text = "Merhaba, ${userData?.firstName ?: "Kullanıcı"} 👋",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (dailyCalories > 0)
                        "Bugün $dailyCalories kcal yaktın🔥, devam et!💪"
                    else
                        "Bugün için bir challenge seç!💪",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(betweenHeaderAndStats))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_weight),
                            contentDescription = "Kilo",
                            modifier = Modifier.size(50.dp)
                        )
                        Text("${userData?.weight ?: 0} KG",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_fire),
                            contentDescription = "Kalori",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(50.dp)
                        )
                        Text("$dailyCalories kcal",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(betweenStatsAndButtons))

                ChallengeRow(R.drawable.ic_oneshoe, level = "3000 ADIM 🚶‍♂️‍➡️") {
                    navController.navigate("challenge/3000")
                }

                Spacer(modifier = Modifier.height(betweenButtons))

                ChallengeRow(R.drawable.ic_duoshoe, level = "6000 ADIM 💨") {
                    navController.navigate("challenge/6000")
                }

                Spacer(modifier = Modifier.height(betweenButtons))

                ChallengeRow(R.drawable.ic_tripleshoe, level = "10000 ADIM 👟") {
                    navController.navigate("challenge/10000")
                }
            }
        }
    }
}

@Composable
fun ChallengeRow(imageRes: Int, level: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        FloatingActionButton(
            onClick = onClick,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(90.dp)
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier.size(90.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(level, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
    }
}



@Composable
fun WeightAndCalorieRow(
    currentWeight: Int,

    dailyCalories: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WeightInfo(label = "Mevcut Kilo", value = "$currentWeight KG", icon = R.drawable.ic_weight)

        DailyCalorieSection(calorie = dailyCalories)


    }
}

@Composable
fun WeightInfo(label: String, value: String, icon: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(painter = painterResource(id = icon), contentDescription = label, modifier = Modifier.size(40.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DailyCalorieSection(calorie: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(painter = painterResource(id = R.drawable.ic_fire), contentDescription = "Kalori", tint = Color.Red, modifier = Modifier.size(40.dp))
        Text(text = "Yakılan Kalori", style = MaterialTheme.typography.bodyMedium)
        Text(text = "$calorie kcal", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}




