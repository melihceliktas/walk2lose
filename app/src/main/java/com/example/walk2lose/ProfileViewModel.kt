package com.example.walk2lose

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow



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
}