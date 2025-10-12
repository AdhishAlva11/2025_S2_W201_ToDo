package com.autgroup.s2025.w201.todo.classes

import com.google.firebase.auth.FirebaseAuth

class SignUpViewModel(private val firebaseAuth: FirebaseAuth? = null) {

    fun validate(email: String?, password: String?, confirmPassword: String?): String? {
        /*if (email.isNullOrBlank()) return "Email cannot be empty"
        if (!isValidEmail(email)) return "Invalid email format"
        if (password.isNullOrBlank()) return "Password cannot be empty"
        if (password.length < 6) return "Password must be at least 6 characters"
        if (password != confirmPassword) return "Passwords do not match"*/
        return null
    }

    private fun isValidEmail(email: String): Boolean {

        //return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        return false
    }

    fun registerWithFirebase(email: String, password: String) {
        //firebaseAuth?.createUserWithEmailAndPassword(email, password)
    }
}