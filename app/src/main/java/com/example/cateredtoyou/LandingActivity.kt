package com.example.cateredtoyou

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class LandingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        // Assuming you have a button with ID continue_button in your layout
        val continueButton: Button = findViewById(R.id.continue_button)
        continueButton.setOnClickListener {
            // Start the LoginScreen activity
            val intent = Intent(this, LoginScreen::class.java)
            startActivity(intent)
        }
    }
}
