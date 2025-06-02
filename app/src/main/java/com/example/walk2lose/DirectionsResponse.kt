package com.example.walk2lose

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DirectionsResponse(
    val routes: List<Route>
)

@JsonClass(generateAdapter = true)
data class Route(
    @Json(name = "overview_polyline")
    val overviewPolyline: OverviewPolyline,
    val legs: List<Leg>
)

@JsonClass(generateAdapter = true)
data class Leg(
    val distance: Distance,
    val duration: Duration
)

@JsonClass(generateAdapter = true)
data class Distance(
    val text: String,
    val value: Int
)

@JsonClass(generateAdapter = true)
data class Duration(
    val text: String,
    val value: Int
)

@JsonClass(generateAdapter = true)
data class OverviewPolyline(
    val points: String
)

