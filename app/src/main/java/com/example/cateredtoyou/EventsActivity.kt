package com.example.cateredtoyou
import com.example.cateredtoyou.R
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class EventsActivity : AppCompatActivity() {

    private lateinit var eventNameInput: EditText
    private lateinit var eventDateInput: EditText
    private lateinit var eventLocationInput: EditText
    private lateinit var clientNameInput: EditText
    private lateinit var expectedGuestsInput: EditText
    private lateinit var statusSpinner: Spinner
    private lateinit var addEventButton: Button
    private lateinit var eventsList: ListView
    private lateinit var events: ArrayList<Event>
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)

        eventNameInput = findViewById(R.id.event_name_input)
        eventDateInput = findViewById(R.id.event_date_input)
        eventLocationInput = findViewById(R.id.event_location_input)
        clientNameInput = findViewById(R.id.client_name_input)
        expectedGuestsInput = findViewById(R.id.expected_guests_input)
        statusSpinner = findViewById(R.id.status_spinner)
        addEventButton = findViewById(R.id.add_event_button)
        eventsList = findViewById(R.id.events_list)

        events = ArrayList()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, events.map { it.toString() })
        eventsList.adapter = adapter

        setupStatusSpinner()

        addEventButton.setOnClickListener {
            if (validateInputs()) {
                addEvent()
            }
        }

        eventsList.setOnItemLongClickListener { _, _, position, _ ->
            showEventOptions(position)
            true
        }
    }

    private fun setupStatusSpinner() {
        ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            Event.EventStatus.values()
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            statusSpinner.adapter = adapter
        }
    }

    private fun validateInputs(): Boolean {
        if (eventNameInput.text.isBlank() || eventDateInput.text.isBlank() ||
            eventLocationInput.text.isBlank() || clientNameInput.text.isBlank() ||
            expectedGuestsInput.text.isBlank()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!isValidDate(eventDateInput.text.toString())) {
            Toast.makeText(this, "Please enter a valid date (DD/MM/YYYY)", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun addEvent() {
        val name = eventNameInput.text.toString()
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(eventDateInput.text.toString())!!
        val location = eventLocationInput.text.toString()
        val status = statusSpinner.selectedItem as Event.EventStatus
        val clientName = clientNameInput.text.toString()
        val expectedGuests = expectedGuestsInput.text.toString().toInt()

        val newEvent = Event(name, date, location, status, clientName, expectedGuests)
        events.add(newEvent)
        adapter.clear()
        adapter.addAll(events.map { it.toString() })
        clearInputs()
        Toast.makeText(this, "Event added", Toast.LENGTH_SHORT).show()
    }

    private fun clearInputs() {
        eventNameInput.text.clear()
        eventDateInput.text.clear()
        eventLocationInput.text.clear()
        clientNameInput.text.clear()
        expectedGuestsInput.text.clear()
        statusSpinner.setSelection(0)
    }

    private fun showEventOptions(position: Int) {
        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(this)
            .setTitle("Event Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editEvent(position)
                    1 -> deleteEvent(position)
                }
            }
            .show()
    }

    private fun editEvent(position: Int) {
        // Implement edit functionality
        Toast.makeText(this, "Edit functionality to be implemented", Toast.LENGTH_SHORT).show()
    }

    private fun deleteEvent(position: Int) {
        events.removeAt(position)
        adapter.clear()
        adapter.addAll(events.map { it.toString() })
        Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show()
    }

    private fun isValidDate(date: String): Boolean {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.isLenient = false
        return try {
            sdf.parse(date)
            true
        } catch (e: Exception) {
            false
        }
    }
}