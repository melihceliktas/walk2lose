package com.example.walk2lose

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChallengeViewModel : ViewModel() {
    private val _targetLocation = MutableStateFlow<LatLng?>(null)
    val targetLocation: StateFlow<LatLng?> = _targetLocation.asStateFlow()

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }


    fun setTargetLocation(location: LatLng) {
        _targetLocation.value = location
    }

    }