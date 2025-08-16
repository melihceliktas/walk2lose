package com.example.walk2lose

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.JointType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Random
import kotlin.math.*
import androidx.compose.runtime.collectAsState as collectAsState1


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChallengeScreen(
    selectedSteps: Int,
    viewModel: ChallengeViewModel = viewModel(),

    udviewModel: ProfileViewModel = viewModel(),

    navController: NavController
) {
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val cameraPositionState = rememberCameraPositionState()
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val targetLocation by viewModel.targetLocation.collectAsState1()
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    val apiKey = BuildConfig.MAPS_API_KEY




    var totalDistanceWalked by remember { mutableStateOf(0.0) }
    var lastLocation by remember { mutableStateOf<LatLng?>(null) }
    var totalDistance by remember { mutableStateOf(0) }
    var totalDuration by remember { mutableStateOf(0)}

    val isPaused by viewModel.isPaused.collectAsState1()

   // saniye
    var challengeStarted by remember { mutableStateOf(false) }

    var isRouteLoading by remember { mutableStateOf(false) }

    val elapsed by viewModel.elapsedTime.collectAsState1()

    var reachedTarget by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {

    }

    LaunchedEffect(Unit) { udviewModel.loadUserData() }

    LaunchedEffect(Unit) {
        viewModel.resetTimer()
    }

    // Location updates
    DisposableEffect(lifecycleOwner) {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.lastOrNull()?.let { location ->
                    userLocation = LatLng(location.latitude, location.longitude)

                    if (userLocation != null && targetLocation != null) {
                        val bearing = calculateBearing(userLocation!!, targetLocation!!)
                        val newCameraPosition = CameraPosition(
                            userLocation!!,
                            15f,
                            0f,
                            bearing
                        )
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.newCameraPosition(newCameraPosition)
                            )
                        }
                    }
                }
            }
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(3000L)
            .build()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    // Kullanıcının yürüdüğü mesafe
    LaunchedEffect(userLocation) {
        if (!isPaused && lastLocation != null && userLocation != null) {
            val dist = FloatArray(1)
            Location.distanceBetween(
                lastLocation!!.latitude, lastLocation!!.longitude,
                userLocation!!.latitude, userLocation!!.longitude,
                dist
            )
            totalDistanceWalked += dist[0]
        }
        lastLocation = userLocation
    }


    if (elapsed % 60 <= 1.01) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                Text("En uygun rota bulunuyor...", color = Color.White, fontSize = 18.sp)
            }
        }
    }

    // Hedef konum belirleme
    LaunchedEffect(userLocation) {
        if (userLocation != null && targetLocation == null) {

            val radius = selectedSteps * 0.7 / 2

            // IO thread'de çalıştır (uzun süren işlem gibi davransın)

            val randomLoc = generateValidTarget(userLocation!!, selectedSteps, apiKey)
            if (randomLoc != null) viewModel.setTargetLocation(randomLoc)


        }
    }




    var distanceText by remember { mutableStateOf("") }
    var durationText by remember { mutableStateOf("") }
    var caloriesText by remember { mutableStateOf("") }

    LaunchedEffect(routePoints) {
        if (routePoints.isNotEmpty() && !isPaused ) {

            viewModel.startTimer()
        } else {
            viewModel.stopTimer()
        }
    }

    // Directions API çağrısı
    LaunchedEffect(userLocation, targetLocation) {
        if (userLocation != null && targetLocation != null) {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com/maps/api/")
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()

                val directionsApi = retrofit.create(DirectionsApi::class.java)

                val toTargetResponse = directionsApi.getDirections(
                    origin = "${userLocation!!.latitude},${userLocation!!.longitude}",
                    destination = "${targetLocation!!.latitude},${targetLocation!!.longitude}",
                    apiKey = apiKey,
                    mode = "walking"
                )

                val toStartResponse = directionsApi.getDirections(
                    origin = "${targetLocation!!.latitude},${targetLocation!!.longitude}",
                    destination = "${userLocation!!.latitude},${userLocation!!.longitude}",
                    apiKey = apiKey,
                    mode = "walking"
                )

                val points1 = toTargetResponse.routes.firstOrNull()?.overviewPolyline?.points
                val points2 = toStartResponse.routes.firstOrNull()?.overviewPolyline?.points

                val decoded1 = if (points1 != null) decodePolyline(points1) else emptyList()
                val decoded2 = if (points2 != null) decodePolyline(points2) else emptyList()

                routePoints = decoded1 + decoded2

                val leg1 = toTargetResponse.routes.firstOrNull()?.legs?.firstOrNull()
                val leg2 = toStartResponse.routes.firstOrNull()?.legs?.firstOrNull()

                val dist1 = leg1?.distance?.value ?: 0
                val dist2 = leg2?.distance?.value ?: 0
                totalDistance = dist1 + dist2

                if (totalDistance > 0) {
                    distanceText = "%.2f km".format(totalDistance / 1000.0)
                }

                totalDuration = (leg1?.duration?.value ?: 0) + (leg2?.duration?.value ?: 0)
                val minutes = totalDuration / 60
                durationText = "$minutes dakika"

                val userWeightKg = udviewModel.userData.value?.weight ?: 0
                val MET = 3.5
                val caloriesBurned = (MET * 3.5 * userWeightKg / 200) * minutes
                caloriesText = "%.0f kcal".format(caloriesBurned)

                /*if (isUserAtTarget(userLocation!!, targetLocation!!)) {
                    udviewModel.updateCaloriesBurned(caloriesBurned.toInt())
                    udviewModel.saveDailyStats(selectedSteps, caloriesBurned.toInt())
                    navController.navigate("finish/$selectedSteps/${caloriesBurned.toInt()}") {
                        popUpTo("challenge/$selectedSteps") { inclusive = true }
                    }
                }*/

            } catch (e: Exception) {
                Log.e("Directions", "Error fetching route", e)
            }
        }
    }

    val contextx = LocalContext.current

    // Progress hesaplama
    val progress = when {
        totalDistance <= 0 -> 0.0
        !reachedTarget -> (totalDistanceWalked / (totalDistance / 2)).coerceIn(0.0, 1.0) * 0.5
        else -> 0.5 + ((totalDistanceWalked - (totalDistance / 2)) / (totalDistance / 2)).coerceIn(0.0, 1.0) * 0.5
    }

    LaunchedEffect(userLocation, targetLocation, totalDistanceWalked) {
        if (!reachedTarget && userLocation != null && targetLocation != null) {
            if (isUserAtTarget(userLocation!!, targetLocation!!)) {
                reachedTarget = true
            }
        }
    }

    LaunchedEffect(progress) {
        if (progress >= 1.0 && userLocation != null && targetLocation != null) {
            val userWeightKg = udviewModel.userData.value?.weight ?: 0
            val MET = 3.5
            val caloriesBurned = (MET * 3.5 * userWeightKg / 200) * (totalDuration / 60)

            val minutes = elapsed / 60
            val seconds = elapsed % 60
            val formattedDuration = String.format("%02d:%02d", minutes, seconds)

            udviewModel.updateCaloriesBurned(caloriesBurned.toInt())
            udviewModel.saveDailyStats(selectedSteps, caloriesBurned.toInt())

            viewModel.stopTimer()

            navController.navigate("finish/$selectedSteps/${caloriesBurned.toInt()}/$formattedDuration") {
                popUpTo("challenge/$selectedSteps") { inclusive = true }
            }
        }
    }


    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true),
                uiSettings = MapUiSettings(zoomControlsEnabled = true),
            ) {
                userLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Senin Konumun",
                        icon = contextx.bitmapDescriptorFromRes(R.drawable.ic_user_marker)
                    )
                }
                targetLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Hedef",
                        icon = contextx.bitmapDescriptorFromRes(R.drawable.ic_target_marker)
                    )
                }
                if (routePoints.isNotEmpty()) {
                    val half = routePoints.size / 2
                    val toTargetPoints = routePoints.subList(0, half)
                    val toStartPoints = routePoints.subList(half, routePoints.size)

                    if (!reachedTarget) {
                        // Gidiş: Kırmızı
                        Polyline(
                            points = toTargetPoints,
                            color = Color(0xFFFF5252).copy(alpha = 0.9f),
                            width = 14f,
                            jointType = JointType.ROUND
                        )
                    } else {
                        // Dönüş: Mavi
                        Polyline(
                            points = toStartPoints,
                            color = Color(0xFF3F51B5).copy(alpha = 0.85f),
                            width = 14f,
                            jointType = JointType.ROUND
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Mesafe: $distanceText", style = MaterialTheme.typography.titleMedium)
                Text("Tahmini Süre: $durationText", style = MaterialTheme.typography.titleMedium)
                Text("Yaklaşık Kalori: $caloriesText", style = MaterialTheme.typography.titleMedium)

                val minutes = elapsed / 60
                val seconds = elapsed % 60
                val formattedDuration = String.format("%02d:%02d", minutes, seconds)

                Text(
                    "Geçen Süre: $formattedDuration",
                    style = MaterialTheme.typography.titleMedium
                )

                LinearProgressIndicator(
                    progress = progress.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    "${(progress * 100).toInt()}% tamamlandı",
                    style = MaterialTheme.typography.bodyMedium
                )

                // ✅ Test butonu (istersen kaldırabilirsin)
                Button(
                    onClick = { totalDistanceWalked = totalDistance.toDouble() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(
                        "Test",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Button(
                    onClick = { viewModel.togglePause() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        if (isPaused) "Devam Et" else "Durdur",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Button(
                    onClick = {
                        val userWeightKg = udviewModel.userData.value?.weight ?: 0
                        val MET = 3.5
                        val caloriesBurned = (MET * 3.5 * userWeightKg / 200) * (elapsed / 60.0)
                        val stepLength = 0.70
                        val currentSteps = (totalDistanceWalked / stepLength).toInt()

                        udviewModel.updateCaloriesBurned(caloriesBurned.toInt())
                        udviewModel.saveDailyStats(currentSteps, caloriesBurned.toInt())
                        viewModel.stopTimer()

                        navController.navigate("incomplete/$currentSteps/${caloriesBurned.toInt()}/$formattedDuration") {
                            popUpTo("challenge/$selectedSteps") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(
                        "Challenge'ı Bitir",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

}}

fun generateRandomLocation(center: LatLng, selectedSteps: Int): LatLng {
    val stepLength = 0.70 // Ortalama adım uzunluğu (metre)
    val totalDistance = selectedSteps * stepLength // Gidiş + dönüş
    val oneWayDistance = totalDistance / 2.0

    // Yaptığım tesltler doğrultusunda 0.75 ve 0.9 en mantıklı çözüm.

    val minRadius = oneWayDistance * 0.75
    val maxRadius = oneWayDistance * 0.9

    val random = java.util.Random()
    val radiusInDegreesMin = minRadius / 111000f
    val radiusInDegreesMax = maxRadius / 111000f

    // Rastgele mesafe [min, max]
    val u = random.nextDouble()
    val v = random.nextDouble()
    val radiusInDegrees = radiusInDegreesMin + (radiusInDegreesMax - radiusInDegreesMin) * u

    val t = 2 * Math.PI * v
    val x = radiusInDegrees * Math.cos(t)
    val y = radiusInDegrees * Math.sin(t)

    val newLat = center.latitude + y
    val newLng = center.longitude + x / Math.cos(Math.toRadians(center.latitude))

    return LatLng(newLat, newLng)
}

fun calculateBearing(start: LatLng, end: LatLng): Float {
    val lat1 = Math.toRadians(start.latitude)
    val lon1 = Math.toRadians(start.longitude)
    val lat2 = Math.toRadians(end.latitude)
    val lon2 = Math.toRadians(end.longitude)

    val dLon = lon2 - lon1
    val y = Math.sin(dLon) * Math.cos(lat2)
    val x = Math.cos(lat1) * Math.sin(lat2) -
            Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon)

    val bearing = Math.toDegrees(Math.atan2(y, x)).toFloat()
    return (bearing + 360) % 360
}



suspend fun generateValidTargetLocation(center: LatLng, radiusMeters: Int): LatLng {
    var target: LatLng
    do {
        target = generateRandomLocation(center, radiusMeters)
    } while (!isRoadNearby(target.latitude, target.longitude))
    return target
}

fun isUserAtTarget(userLoc: LatLng, targetLoc: LatLng): Boolean {
    val distance = calculateDistance(userLoc, targetLoc)
    return distance < 10 // Hedefe 10 metre mesafeye gelirse challenge bitmiş sayılır
}

// Mesafe hesaplama fonksiyonu (iki nokta arasındaki mesafe)
fun calculateDistance(start: LatLng, end: LatLng): Float {
    val results = FloatArray(1)
    Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results)
    return results[0]
}


suspend fun generateValidTarget(
    userLocation: LatLng,
    selectedSteps: Int,
    apiKey: String,
    maxAttempts: Int = 10
): LatLng? {
    val targetDistance = selectedSteps * 0.7 // metre
    val tolerance = targetDistance * 0.1      // %10 tolerans

    repeat(maxAttempts) {
        val randomLoc = generateRandomLocation(userLocation, selectedSteps)

        // Directions API ile rota mesafesi al
        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        val directionsApi = retrofit.create(DirectionsApi::class.java)

        val toTargetResponse = directionsApi.getDirections(
            origin = "${userLocation.latitude},${userLocation.longitude}",
            destination = "${randomLoc.latitude},${randomLoc.longitude}",
            apiKey = apiKey,
            mode = "walking"
        )
        val toStartResponse = directionsApi.getDirections(
            origin = "${randomLoc.latitude},${randomLoc.longitude}",
            destination = "${userLocation.latitude},${userLocation.longitude}",
            apiKey = apiKey,
            mode = "walking",
            alternatives = true

        )

        val leg1 = toTargetResponse.routes.firstOrNull()?.legs?.firstOrNull()
        val leg2 = toStartResponse.routes.firstOrNull()?.legs?.firstOrNull()

        val totalDistance = (leg1?.distance?.value ?: 0) + (leg2?.distance?.value ?: 0)

        // Hedef tolerans içinde mi?
        if (kotlin.math.abs(totalDistance - targetDistance) <= tolerance) {
            return randomLoc
        }
    }

    // Tolerans içinde bir hedef bulunamazsa son üretilen hedefi döndür
    return generateRandomLocation(userLocation, selectedSteps)
}

fun Context.bitmapDescriptorFromRes(resId: Int): BitmapDescriptor {
    val bitmap = BitmapFactory.decodeResource(resources, resId)
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 128, 128, false) // 96x96 güvenli boyut
    return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
}

// Challenge'ı bitirme fonksiyonu
/*fun endChallenge() {
    val totalCaloriesBurned = calculateCalories(selectedSteps)
    udviewModel.updateCaloriesBurned(totalCaloriesBurned)
}*/

fun offsetTarget(latLng: LatLng): LatLng {
    val offsetLat = latLng.latitude + (0.0009 - Math.random() * 0.0004) // ±100m
    val offsetLng = latLng.longitude + (0.0009 - Math.random() * 0.0004)
    return LatLng(offsetLat, offsetLng)
}

