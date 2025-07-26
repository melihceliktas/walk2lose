package com.example.walk2lose

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar


class ProfileViewModel : ViewModel() {
    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    private val _dailyStats = MutableStateFlow<List<DailyStats>>(emptyList())
    val dailyStats: StateFlow<List<DailyStats>> = _dailyStats

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()




    @RequiresApi(Build.VERSION_CODES.O)
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
        loadDailyStats(uid)
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


    // Günlük verileri çekme
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadDailyStats(uid: String?) {
        if (uid == null) return

        firestore.collection("users")
            .document(uid)
            .collection("dailyStats")
            .get()
            .addOnSuccessListener { snapshot ->
                val stats = snapshot.documents.mapNotNull { doc ->
                    val data = doc.toObject(DailyStats::class.java)
                    val timestamp = doc.id.toLongOrNull() ?: return@mapNotNull null

                    data?.copy(date = timestamp)
                }

                // Aynı günün verilerini toplamak için
                val aggregated = stats.groupBy {
                    Instant.ofEpochMilli(it.date)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .toEpochDay()
                }.map { entry ->
                    DailyStats(
                        date = entry.key, // epochDay formatında
                        steps = entry.value.sumOf { it.steps },
                        caloriesBurned = entry.value.sumOf { it.caloriesBurned }
                    )
                }

                _dailyStats.value = aggregated
                Log.d("ProfileVM", "Loaded daily stats: $aggregated")
            }
            .addOnFailureListener { e ->
                Log.e("ProfileViewModel", "Error loading daily stats", e)
            }
    }

    fun saveDailyStats(steps: Int, caloriesBurned: Int) {
        val uid = auth.currentUser?.uid ?: return
        val date = Calendar.getInstance().timeInMillis // Günün tarihini alıyoruz

        val dailyStats = hashMapOf(
            "steps" to steps,
            "caloriesBurned" to caloriesBurned
        )

        firestore.collection("users")
            .document(uid)
            .collection("dailyStats")
            .document(date.toString())  // Veriyi tarih ile kaydediyoruz
            .set(dailyStats)
            .addOnSuccessListener {
                Log.d("ProfileViewModel", "Daily stats saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("ProfileViewModel", "Error saving daily stats", e)
            }

}}