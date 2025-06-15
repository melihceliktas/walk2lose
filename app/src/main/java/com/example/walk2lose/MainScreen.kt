package com.example.walk2lose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar

import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon

import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Scaffold

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
fun MainScreen(


    dailyCalories: Int,

    navController: NavHostController,

    viewModel: ProfileViewModel = viewModel()

) {



    LaunchedEffect(Unit) { viewModel.loadUserData() }


    Scaffold(

        topBar =  { },

        bottomBar = {

        },
        floatingActionButton = { // Ortadaki büyük buton
            FloatingActionButton(
                onClick = { navController.navigate("challenge/10000") },
                shape = RoundedCornerShape(50), // Oval şekil
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier

                    .size(80.dp) // Ortadaki butonun boyutu
                    .offset(y = (-250).dp) // Ortadaki butonun daha yukarıda olması
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_10k),
                    contentDescription = "Challenge",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp) // İkon boyutu
                )
            }


            FloatingActionButton(onClick = { navController.navigate("challenge/6000") },
                shape = RoundedCornerShape(50), // Oval şekil
                containerColor = MaterialTheme.colorScheme.primary,

                modifier = Modifier
                    .size(80.dp) // Ortadaki butonun boyutu
                    .offset(x = (-100).dp,y = (-350).dp))

                {

                Icon(
                    painter = painterResource(id = R.drawable.ic_6k),
                contentDescription = "Challenge",
                tint = Color.White,
                modifier = Modifier.size(40.dp) // İkon boyutu
            )
            }
            FloatingActionButton(onClick = { navController.navigate("challenge/3000") },
                shape = RoundedCornerShape(50), // Oval şekil
                containerColor = MaterialTheme.colorScheme.primary,

                modifier = Modifier
                    .size(80.dp) // Ortadaki butonun boyutu
                    .offset(y = (-450).dp))

            {

                Icon(
                    painter = painterResource(id = R.drawable.ic_3k),
                    contentDescription = "Challenge",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp) // İkon boyutu
                )
            }




        },

        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Diğer butonlar
            /*OvalButton(
                onClick = { navController.navigate("main") },
                icon = Icons.Default.Home,
                contentDescription = "Ana Sayfa",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 32.dp, y = (-32).dp) // Sol altta, yukarıya kaydırılmış
            )*/

            OvalButton(
                onClick = { navController.navigate("profile") /*onNavigate("profile")*/ },
                icon = Icons.Default.Person,
                contentDescription = "Profil",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-32).dp, y = (-64).dp) // Sağ altta, yukarıya kaydırılmış
            )



            // İçerik
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .offset(y=(32).dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                val userWeightKg = viewModel.userData.value?.weight ?: 0

                WeightAndCalorieRow(
                    currentWeight = userWeightKg,

                    dailyCalories = dailyCalories
                )

                Spacer(modifier = Modifier.height(40.dp))

            }
        }
    }
}

@Composable
fun OvalButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        shape = RoundedCornerShape(50), // Oval şekil
        containerColor = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .size(75.dp) // Boyutları
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(30.dp) // İkon boyutu
        )
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
        Text(text = "Günlük", style = MaterialTheme.typography.bodyMedium)
        Text(text = "$calorie kcal", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

/*@Composable
fun RemainingDaysSection(daysLeft: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Kalan Gün Sayısı",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "$daysLeft Gün",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}*/



