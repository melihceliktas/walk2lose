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
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.*
import androidx.compose.runtime.collectAsState as collectAsState1

@Composable
fun ChallengeScreen(
    selectedSteps: Int,
    viewModel: ChallengeViewModel = viewModel(),

    udviewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val cameraPositionState = rememberCameraPositionState()
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val targetLocation by viewModel.targetLocation.collectAsState1()
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val apiKey = "AIzaSyAHBvqnRsi-f0zWmAMF42xQ7o35cCEkK34"

    var isFindingTarget by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { udviewModel.loadUserData() }

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
            }}

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

    // Hedef konumu sadece userLocation geldiyse ve targetLocation yoksa ViewModel’a set et
    LaunchedEffect(userLocation) {
        if (userLocation != null && targetLocation == null) {
            //viewModel.setLoading(true)

            val radius = selectedSteps * 0.7 / 2


            //Accurate Path finding function is bellow however I'm using a free api to check
            //if there is a road nearby or not, I can't use it all time

            //val randomLoc = generateValidTargetLocation(userLocation!!, radius.toInt())
            val randomLoc = generateRandomLocation(userLocation!!, radius.toInt())
            viewModel.setTargetLocation(randomLoc)

            //viewModel.setLoading(false)
        }
    }

    var distanceText by remember { mutableStateOf("") }
    var durationText by remember { mutableStateOf("") }
    var caloriesText by remember { mutableStateOf("") }
    // Directions API çağrısı
    LaunchedEffect(userLocation, targetLocation) {
        if (userLocation != null && targetLocation != null) {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com/maps/api/")
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()

                val directionsApi = retrofit.create(DirectionsApi::class.java)

                // İlk rota: Başlangıç -> Hedef
                val toTargetResponse = directionsApi.getDirections(
                    origin = "${userLocation!!.latitude},${userLocation!!.longitude}",
                    destination = "${targetLocation!!.latitude},${targetLocation!!.longitude}",
                    apiKey = apiKey,
                    mode = "walking"
                )

                // İkinci rota: Hedef -> Başlangıç
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

                val totalDistance = (leg1?.distance?.value)?.plus((leg2?.distance?.value!!))
                val totalDuration = (leg1?.duration?.value)?.plus((leg2?.duration?.value!!))

                if (totalDistance != null) {
                    distanceText = "%.2f km".format(totalDistance/ 1000.0)
                }


                val minutes = totalDuration?.div(60)

                durationText ="$minutes dakika"


                val userWeightKg = udviewModel.userData.value?.weight ?: 0

                val MET = 3.5
                val caloriesBurned = (MET * 3.5 * userWeightKg / 200) * minutes!!


                caloriesText = "%.0f kcal".format(caloriesBurned)


            } catch (e: Exception) {
                Log.e("Directions", "Error fetching roundtrip directions", e)
            }
        }
    }


    Column(modifier = Modifier.fillMaxSize()) {
    Box(modifier = Modifier
        .weight(1f)
        .fillMaxWidth()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(zoomControlsEnabled = true)
        ) {
            userLocation?.let { Marker(state = MarkerState(position = it), title = "Senin Konumun") }
            targetLocation?.let { Marker(state = MarkerState(position = it), title = "Hedef") }
            if (routePoints.isNotEmpty()) {
                Polyline(points = routePoints, color = Color.Blue, width = 6f)

            }
        }
    }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color(0xFFF0F0F0))
                .padding(16.dp)
        ) {
            Text(text = "Mesafe: $distanceText", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Tahmini Süre: $durationText", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Yaklaşık Kalori: $caloriesText", style = MaterialTheme.typography.bodyLarge)
        }
}}

fun generateRandomLocation(center: LatLng, selectedSteps: Int): LatLng {
    val stepLength = 0.75 // Ortalama adım uzunluğu (metre)
    val totalDistance = selectedSteps * stepLength // Gidiş + dönüş
    val oneWayDistance = totalDistance / 2.0

    // Min ve max mesafe (tek yön) %5 tolerans ile
    val minRadius = oneWayDistance * 0.95
    val maxRadius = oneWayDistance * 1.05

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