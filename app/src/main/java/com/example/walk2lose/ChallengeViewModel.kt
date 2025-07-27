package com.example.walk2lose

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ChallengeViewModel : ViewModel() {
    private val _targetLocation = MutableStateFlow<LatLng?>(null)
    val targetLocation: StateFlow<LatLng?> = _targetLocation.asStateFlow()

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading


    private val _elapsedTime = MutableStateFlow(0)
    val elapsedTime: StateFlow<Int> = _elapsedTime

    private var job: Job? = null

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }


    fun setTargetLocation(location: LatLng) {
        _targetLocation.value = location
    }

    private var started = false

    fun startTimer() {
        if (started) return   //
        started = true
        job?.cancel()
        job = viewModelScope.launch {
            val ticker = ticker(delayMillis = 1000) //ticker işe yaradı onun dışında crash yiyordum
            for (event in ticker){

                if(!_isPaused.value){

                _elapsedTime.value += 1
            }}
        }
    }

    fun stopTimer() {
        job?.cancel()
        started = false
    }

    fun resetTimer() {
        _elapsedTime.value = 0
    }


    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused

    fun togglePause() {
        _isPaused.value = !_isPaused.value
    }

    fun setPause(value: Boolean) {
        _isPaused.value = value
    }
}