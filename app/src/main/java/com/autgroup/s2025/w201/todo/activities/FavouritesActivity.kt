package com.autgroup.s2025.w201.todo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import com.autgroup.s2025.w201.todo.R

class FavouritesActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inject Favourites layout into BaseActivity skeleton
        layoutInflater.inflate(R.layout.activity_favourites, findViewById(R.id.content_frame))

        // Highlight Favourites tab in bottom navigation
        setupBottomNavigation(R.id.nav_favourites)

        // Back button → optional (since bottom nav already handles Home)
        val backButton: ImageButton = findViewById(R.id.btnBack)
        backButton.setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
            // finish() not strictly needed, but can be kept if you don’t want this screen in back stack
        }

        // Add Favourite action (stub)
        val addFavourite: TextView = findViewById(R.id.tvAddFavourite)
        addFavourite.setOnClickListener {
            println("Add Favourite clicked (not functional yet)")
        }
    }
}
