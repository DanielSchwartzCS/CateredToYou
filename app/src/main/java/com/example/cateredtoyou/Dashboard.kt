package com.example.cateredtoyou

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Dashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Find the events button and set up navigation to Events page
        val eventsButton: Button = findViewById(R.id.eventsButton)
        eventsButton.setOnClickListener {
            val intent = Intent(this, Events::class.java)
            startActivity(intent)
        }

        val inventoryButton: Button = findViewById(R.id.inventoryButton)
        inventoryButton.setOnClickListener {
            val intent = Intent(this, Inventory::class.java)
            startActivity(intent)
        }
    }
}
