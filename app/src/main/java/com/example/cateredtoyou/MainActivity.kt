package com.example.cateredtoyou

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.app.ActivityOptions

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnInventory: Button = findViewById(R.id.btn_inventory)
        btnInventory.setOnClickListener {
            val intent = Intent(this, InventoryActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this, R.anim.slide_in_right, R.anim.slide_out_left
            )
            startActivity(intent, options.toBundle())
        }

        val btnEvents: Button = findViewById(R.id.btn_events)
        btnEvents.setOnClickListener {
            val intent = Intent(this, EventsActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this, R.anim.slide_in_right, R.anim.slide_out_left
            )
            startActivity(intent, options.toBundle())
        }

        val btnUsers: Button = findViewById(R.id.btn_users)
        btnUsers.setOnClickListener {
            val intent = Intent(this, UserActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this, R.anim.slide_in_right, R.anim.slide_out_left
            )
            startActivity(intent, options.toBundle())
        }

        val btnRecipes: Button = findViewById(R.id.btn_recipes)
        btnRecipes.setOnClickListener {
            val intent = Intent(this, RecipeActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this, R.anim.slide_in_right, R.anim.slide_out_left
            )
            startActivity(intent, options.toBundle())
        }

        val btnEventsView: Button = findViewById(R.id.btn_eventsview)
        btnEventsView.setOnClickListener {
            val intent = Intent(this, EventsView::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this, R.anim.slide_in_right, R.anim.slide_out_left
            )
            startActivity(intent, options.toBundle())
        }

        val btnTaskView: Button = findViewById(R.id.btn_taskview)
        btnTaskView.setOnClickListener {
            val intent = Intent(this, TaskView::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this, R.anim.slide_in_right, R.anim.slide_out_left
            )
            startActivity(intent, options.toBundle())
        }
    }
}
