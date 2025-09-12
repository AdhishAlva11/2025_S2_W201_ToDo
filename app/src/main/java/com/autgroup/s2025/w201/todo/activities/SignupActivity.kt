package com.autgroup.s2025.w201.todo.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.autgroup.s2025.w201.todo.classes.Activity
import com.autgroup.s2025.w201.todo.classes.Favourities
import com.autgroup.s2025.w201.todo.classes.User
import com.autgroup.s2025.w201.todo.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.textView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener {
            val firstName = binding.firstNameEt.text.toString()
            val lastName = binding.lastNameEt.text.toString()
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()
            val confirmPass = binding.confirmPassEt.text.toString()

            if(email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()){
                if(pass == confirmPass){
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if(it.isSuccessful){

                            val user = firebaseAuth.currentUser
                            val userId = user?.uid ?: return@addOnCompleteListener

                            var dbRef = FirebaseDatabase.getInstance(
                                "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
                            ).getReference(userId)

                            val userData = User(firstName, lastName, null, null)

                            dbRef.child("UserData").setValue(userData)
                                .addOnSuccessListener {
                                    Log.d("FirebaseTest", "Test write success")
                                    Toast.makeText(this, "Test write success", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FirebaseTest", "Test write failed", e)
                                    Toast.makeText(this, "Test write failed: ${e.message}", Toast.LENGTH_LONG).show()
                                }

                            /*val userData = mapOf(
                                "favourites" to mapOf<String, Any>(),
                                "itineraries" to mapOf<String, Any>()
                            )

                            dbRef = FirebaseDatabase.getInstance(
                                "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
                            ).getReference("users/$userId")

                            dbRef.child("userData").setValue(userData)*/

                            val intent = Intent(this, HomePageActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Password is not matching", Toast.LENGTH_LONG).show()
                }

            } else {
                Toast.makeText(this, "Empty Fields Are Not Allowed", Toast.LENGTH_LONG).show()
            }
        }
    }
}