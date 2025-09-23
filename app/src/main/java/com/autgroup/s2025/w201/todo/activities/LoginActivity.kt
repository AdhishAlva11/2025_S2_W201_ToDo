package com.autgroup.s2025.w201.todo.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.classes.User
import com.autgroup.s2025.w201.todo.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: com.google.android.gms.auth.api.signin.GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // Navigate to signup
        binding.textView.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        // Email/password login
        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {   // both must be filled
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this, HomePageActivity::class.java)
                        startActivity(intent)
                        finish()  // prevent going back to login
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Empty Fields Are Not Allowed", Toast.LENGTH_LONG).show()
            }
        }

        // Google Sign-In options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Google Sign-In button
        findViewById<Button>(R.id.googleSigninButton).setOnClickListener {
            signInGoogle()
        }

        binding.tvForgotPassword.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email to reset password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Reset link sent to $email. Check your inbox.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    // Trigger Google Sign-In flow
    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    // Handle Google Sign-In result
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        }
    }

    // Process sign-in result
    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                updateUI(account)
            }
        } else {
            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_LONG).show()
        }
    }

    // Update UI after Google login
    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {

                val user = firebaseAuth.currentUser
                val userId = user?.uid ?: return@addOnCompleteListener

                val fullName = account.displayName ?: ""
                val firstLastName = fullName.split(" ")
                val firstNameFromGoogle = firstLastName.firstOrNull() ?: ""
                val lastNameFromGoogle = if (firstLastName.size > 1) {
                    firstLastName.subList(1, firstLastName.size).joinToString(" ")
                } else ""

                val dbRef = FirebaseDatabase.getInstance(
                    "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
                ).getReference(userId)

                // Extract email and photo from Google account
                val emailFromGoogle = account.email ?: ""
                val photoUrlFromGoogle = account.photoUrl?.toString()

                //added this  - saves photo
                val userData = User(
                    userFirstName = firstNameFromGoogle,
                    userLastName = lastNameFromGoogle,
                    email = emailFromGoogle,
                    photoUrl = photoUrlFromGoogle,
                    userFavourities = null,
                    userItineraries = null
                )

                // Prevent overwriting existing user data
                dbRef.child("UserData").get().addOnSuccessListener { snapshot ->
                    if (!snapshot.exists()) {
                        dbRef.child("UserData").setValue(userData)
                            .addOnSuccessListener {
                                Log.d("FirebaseTest", "UserData created successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e("FirebaseTest", "UserData write failed", e)
                            }
                    } else {
                        Log.d("FirebaseTest", "UserData already exists, not overwriting")
                    }

                    val intent = Intent(this, HomePageActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            } else {
                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Auto-login if user is already signed in
        if (firebaseAuth.currentUser != null) {
            val intent = Intent(this, HomePageActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
