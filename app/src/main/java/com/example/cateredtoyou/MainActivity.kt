package com.example.cateredtoyou

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnInventory: Button = findViewById(R.id.btn_inventory)
        btnInventory.setOnClickListener {
            val intent = Intent(this, InventoryActivity::class.java)
            startActivity(intent)
        }

        val btnEvents: Button = findViewById(R.id.btn_events)
        btnEvents.setOnClickListener {
            val intent = Intent(this, EventsActivity::class.java)
            startActivity(intent)
        }

        val btnUsers: Button = findViewById(R.id.btn_users)
        btnUsers.setOnClickListener {
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
        }

        // New button for Recipes
        val btnRecipes: Button = findViewById(R.id.btn_recipes)
        btnRecipes.setOnClickListener {
            val intent = Intent(this, RecipeActivity::class.java)
            startActivity(intent)
        }

        // New button for EventsView
        val btnEventsView: Button = findViewById(R.id.btn_eventsview)
        btnEventsView.setOnClickListener {
            val intent = Intent(this, EventsView::class.java)
            startActivity(intent)
        }
    }
}
