package com.example.walk2lose



import android.app.Person
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.time.LocalDate


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadUserData()
    }

    val userData by viewModel.userData.collectAsState()
    val dailyStats by viewModel.dailyStats.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("edit_profile") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Profili Düzenle")
            }
        }
    ) { paddingValues ->

        ProfileAnimatedBackground()

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val screenHeight = maxHeight
            val screenWidth = maxWidth

            if (userData == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = screenWidth * 0.05f), // Ekranın %5'i kadar yan boşluk
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(screenHeight * 0.03f) // Elemanlar arası %3 boşluk
                ) {
                    // ✅ Yukarıya ekran yüksekliğinin %10'u kadar boşluk
                    Spacer(modifier = Modifier.height(screenHeight * 0.1f))

                    ProfileInfoCard(userData!!, screenHeight)
                    CalendarCard(dailyStats, screenHeight)
                }
            }
        }
    }
}

@Composable
fun ProfileInfoCard(userData: UserData, screenHeight: Dp) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(screenHeight * 0.26f), // Kart yüksekliği ekranın %25'i
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = screenHeight * 0.02f), // Dikey padding oran bazlı
            verticalArrangement = Arrangement.spacedBy(screenHeight * 0.015f)
        ) {
            Text(
                text = "${userData.firstName} ${userData.lastName}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Divider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f))

            ProfileInfoRow("E-posta", userData.email ?: "-")
            ProfileInfoRow("Yaş", userData.age?.toString() ?: "-")
            ProfileInfoRow("Boy", "${userData.height ?: "-"} cm")
            ProfileInfoRow("Kilo", "${userData.weight ?: "-"} kg")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarCard(dailyStats: List<DailyStats>, screenHeight: Dp) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(screenHeight * 0.45f), // Takvim kartı ekranın %45'i
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(screenHeight * 0.02f) // Takvim içi padding ekran oranına göre
        ) {
            CalendarView(dailyStats = dailyStats)
        }
    }
}

@Composable
fun ProfileHeader(userData: UserData) {
    Text(
        text = "${userData.firstName} ${userData.lastName}",
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        color = Color.White
    )
}

@Composable
fun ProfileInfoCard(userData: UserData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "${userData.firstName} ${userData.lastName}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Divider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f))

            ProfileInfoRow("E-posta", userData.email ?: "-")
            ProfileInfoRow("Yaş", userData.age?.toString() ?: "-")
            ProfileInfoRow("Boy", "${userData.height ?: "-"} cm")
            ProfileInfoRow("Kilo", "${userData.weight ?: "-"} kg")
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarCard(dailyStats: List<DailyStats>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CalendarView(dailyStats = dailyStats)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarView(
    dailyStats: List<DailyStats>
) {
    val today = LocalDate.now()
    var currentMonth by remember { mutableStateOf(today.withDayOfMonth(1)) }

    val daysOfWeek = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
    val daysInMonth = currentMonth.lengthOfMonth()
    val startOfMonth = LocalDate.of(currentMonth.year, currentMonth.month, 1)

    var firstDayOfWeek = startOfMonth.dayOfWeek.value - 1
    if (firstDayOfWeek < 0) firstDayOfWeek = 6

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Ay başlığı ve ileri/geri butonları
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { currentMonth = currentMonth.minusMonths(1) },
                enabled = true
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Önceki Ay")
            }

            Text(
                text = "${currentMonth.month.name.lowercase().replaceFirstChar { it.titlecase() }} ${currentMonth.year}",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

            IconButton(
                onClick = { currentMonth = currentMonth.plusMonths(1) },
                enabled = currentMonth.monthValue < today.monthValue || currentMonth.year < today.year
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Sonraki Ay",
                    tint = if (currentMonth.monthValue < today.monthValue || currentMonth.year < today.year)
                        LocalContentColor.current
                    else Color.Gray
                )
            }
        }

        // Haftanın günleri başlığı
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            daysOfWeek.forEach {
                Text(text = it, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(firstDayOfWeek) {
                Box(modifier = Modifier.padding(2.dp).aspectRatio(1.05f)) { }
            }

            items((1..daysInMonth).toList()) { day ->
                val currentDate = startOfMonth.plusDays(day.toLong() - 1)
                val statsForDay = dailyStats.find { it.date == currentDate.toEpochDay() }

                val isFuture =
                    currentDate.isAfter(today) && currentMonth.month == today.month && currentMonth.year == today.year
                val isToday = currentDate == today

                BoxWithConstraints(
                    modifier = Modifier
                        .padding(1.dp)
                        .aspectRatio(0.85f)
                        .background(
                            if (isToday) MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.secondary,
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    val boxHeight = maxHeight
                    val dayFontSize = (maxWidth.value * 0.3).sp
                    val valueFontSize = (maxWidth.value * 0.18).sp

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Gün numarası
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(boxHeight * 0.33f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                fontSize = dayFontSize,
                                fontWeight = FontWeight.Bold,
                                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Steps
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(boxHeight * 0.33f),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                isFuture -> {}
                                statsForDay != null -> {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                        Icon(
                                            painter = painterResource(id=R.drawable.ic_step),
                                            contentDescription = "Steps",
                                            modifier = Modifier.size(valueFontSize.value.dp * 1.2f),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "${statsForDay.steps/1000}k",
                                            fontSize = valueFontSize,
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                    }
                                }
                                else -> {
                                    Text(
                                        text = "X",
                                        fontSize = valueFontSize,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Calories
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(boxHeight * 0.33f),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                isFuture -> {}
                                statsForDay != null -> {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_fire),
                                            contentDescription = "Calories",
                                            modifier = Modifier.size(valueFontSize.value.dp * 1.2f),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "${statsForDay.caloriesBurned}",
                                            fontSize = valueFontSize,
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                }
                }
                }
        }

@Composable
fun ProfileAnimatedBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(90000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 2f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Image(
        painter = painterResource(R.drawable.person_wallpaper),
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                rotationZ = rotation
                scaleX = scale
                scaleY = scale
            },
        contentScale = ContentScale.Crop
    )
}