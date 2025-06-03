package com.example.walk2lose

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    private val _registerState = MutableStateFlow<Boolean?>(null)
    val registerState = _registerState.asStateFlow()

    fun registerUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        age: Int,
        height: Int,
        weight: Int
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val userMap = hashMapOf(
                        "uid" to uid,
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "email" to email,
                        "age" to age,
                        "height" to height,
                        "weight" to weight,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                    db.collection("users").document(uid).set(userMap)
                        .addOnSuccessListener { _registerState.value = true }
                        .addOnFailureListener { _registerState.value = false }
                } else {
                    _registerState.value = false
                }
            }
    }
}