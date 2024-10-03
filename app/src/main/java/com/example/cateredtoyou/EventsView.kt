package com.example.cateredtoyou

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class EventsView : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Reference the correct layout file
        setContentView(R.layout.activity_eventsview)

        // Handle the back button to return to the previous activity (Dashboard)
        val backButton: Button = findViewById(R.id.back_to_MainActivity)
        backButton.setOnClickListener {
            finish() // Close the current activity and return to Dashboard
        }

        // Dummy event data (this would eventually come from a database)
        val events = listOf(
            "Wedding Catering - 12th Dec",
            "Corporate Event - 15th Dec",
            "Birthday Party - 20th Dec",
            "Holiday Party - 25th Dec"
        )

        // Find the ListView and set up the adapter to display the events
        val eventsListView: ListView = findViewById(R.id.events_list_view)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, events)
        eventsListView.adapter = adapter
    }
}
