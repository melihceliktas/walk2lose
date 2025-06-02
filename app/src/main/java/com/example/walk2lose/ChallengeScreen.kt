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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
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

@Composable
fun ChallengeScreen(
    viewModel: ChallengeViewModel = viewModel()
) {
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val cameraPositionState = rememberCameraPositionState()
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val targetLocation by viewModel.targetLocation.collectAsState()
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val apiKey = "AIzaSyAHBvqnRsi-f0zWmAMF42xQ7o35cCEkK34"

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
            val randomLoc = generateRandomLocation(userLocation!!, 500.0)
            viewModel.setTargetLocation(randomLoc)
        }
    }
    var distanceText by remember { mutableStateOf("") }
    var durationText by remember { mutableStateOf("") }
    // Directions API çağrısı
    LaunchedEffect(userLocation, targetLocation) {
        if (userLocation != null && targetLocation != null) {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com/maps/api/")
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()

                val directionsApi = retrofit.create(DirectionsApi::class.java)

                val response = directionsApi.getDirections(
                    origin = "${userLocation!!.latitude},${userLocation!!.longitude}",
                    destination = "${targetLocation!!.latitude},${targetLocation!!.longitude}",
                    apiKey = apiKey,
                    mode = "walking"
                )

                val points = response.routes.firstOrNull()?.overviewPolyline?.points
                if (points != null) {
                    routePoints = decodePolyline(points)
                }

                val leg = response.routes.firstOrNull()?.legs?.firstOrNull()
                if (leg != null) {
                    distanceText = leg.distance.text ?: ""
                    durationText = leg.duration.text ?: ""
                }

            } catch (e: Exception) {
                Log.e("Directions", "Error fetching directions", e)
            }
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
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
        }
}}

fun generateRandomLocation(center: LatLng, radiusMeters: Double): LatLng {
    val random = java.util.Random()
    val radiusInDegrees = radiusMeters / 111000f

    val u = random.nextDouble()
    val v = random.nextDouble()
    val w = radiusInDegrees * Math.sqrt(u)
    val t = 2 * Math.PI * v
    val x = w * Math.cos(t)
    val y = w * Math.sin(t)

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