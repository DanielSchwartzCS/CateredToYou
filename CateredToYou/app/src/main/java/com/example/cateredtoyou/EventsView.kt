package com.example.cateredtoyou

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class EventsView : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eventsview)

        // Handle the back button to return to the previous activity (Dashboard)
        val backButton: Button = findViewById(R.id.back_to_MainActivity)
        backButton.setOnClickListener {
            finish() // Close the current activity and return to Dashboard
        }

        // Placeholder data for events
        val events = listOf(
            "Event: Wedding Catering\nGuests: 100\nDate: Dec 12",
            "Event: Corporate Event\nGuests: 150\nDate: Dec 15",
            "Event: Birthday Party\nGuests: 50\nDate: Dec 20",
            "Event: Holiday Party\nGuests: 200\nDate: Dec 25"
        )

        // Custom ArrayAdapter to use the event_item layout
        val eventsListView: ListView = findViewById(R.id.events_list_view)
        val adapter = ArrayAdapter(this, R.layout.event_item, R.id.event_details, events)
        eventsListView.adapter = adapter
    }
}
