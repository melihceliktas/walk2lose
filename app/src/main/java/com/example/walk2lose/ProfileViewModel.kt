package com.example.walk2lose

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar


class ProfileViewModel : ViewModel() {
    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()




    fun loadUserData() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            // Kullanıcı giriş yapmamışsa direkt null atabiliriz
            _userData.value = null
            return
        }

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val data = document.toObject(UserData::class.java)
                if (data != null) {
                    _userData.value = data
                } else {
                    // Veri yoksa boş UserData atabiliriz
                    _userData.value = UserData()
                }
            }
            .addOnFailureListener {
                // Hata varsa null veya boş UserData at
                _userData.value = UserData()
            }
    }

    fun updateUserData(
        updatedUser: UserData,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .set(updatedUser)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun updateCaloriesBurned(calories: Int) {
        val currentData = _userData.value
        if (currentData != null) {
            val updatedUser = currentData.copy(caloriesBurned = currentData.caloriesBurned + calories)
            updateUserData(updatedUser, {}, {})
        }
    }

    // Günlük kalori bilgilerini sıfırlama
    fun resetDailyCalories() {
        val updatedUser = _userData.value?.copy(caloriesBurned = 0)
        updatedUser?.let { updateUserData(it, {}, {}) }
    }

    fun startDailyResetTimer() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)  // Akşam 12'de sıfırlama
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)

        val resetTime = calendar.timeInMillis
        val now = System.currentTimeMillis()
        val delayTime = resetTime - now

        // Zamanlayıcıyı başlatıyoruz
        Handler(Looper.getMainLooper()).postDelayed({
            resetDailyCalories()  // Günlük kalori sıfırlama
        }, delayTime)
    }

}