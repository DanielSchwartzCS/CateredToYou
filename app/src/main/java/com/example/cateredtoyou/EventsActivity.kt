package com.example.cateredtoyou

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.cateredtoyou.apifiles.*

import com.google.android.material.progressindicator.CircularProgressIndicator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class EventsActivity : AppCompatActivity() {
    private lateinit var eventNameInput: EditText
    private lateinit var eventDateInput: EditText
    private lateinit var eventStartTimeInput: EditText
    private lateinit var eventEndTimeInput: EditText
    private lateinit var eventLocationInput: EditText
    private lateinit var clientSpinner: Spinner
    private lateinit var expectedGuestsInput: EditText
    private lateinit var statusSpinner: Spinner
    private lateinit var addEventButton: Button
    private lateinit var newClientButton: Button
    private lateinit var selectStaffButton: Button
    private lateinit var menuItemListView: ListView
    private lateinit var equipmentListView: ListView

    private var clients = listOf<Client>()

    companion object {
        private const val TAG = "EventsActivity"
        private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        private val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        private val displayFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)

        try {
            initializeViews()
            setupSpinners()
            setupDateTimePickers()
            setupListeners()
            loadClients()
        } catch (e: Exception) {
            Log.e(TAG, "Error during initialization", e)
            Toast.makeText(this, "Error initializing app", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun initializeViews() {
        try {
            eventNameInput = findViewById<EditText>(R.id.event_name_input)
            eventDateInput = findViewById<EditText>(R.id.event_date_input)
            eventStartTimeInput = findViewById<EditText>(R.id.event_start_time_input)
            eventEndTimeInput = findViewById<EditText>(R.id.event_end_time_input)
            eventLocationInput = findViewById<EditText>(R.id.event_location_input)
            clientSpinner = findViewById<Spinner>(R.id.client_spinner)
            expectedGuestsInput = findViewById<EditText>(R.id.expected_guests_input)
            statusSpinner = findViewById<Spinner>(R.id.status_spinner)
            selectStaffButton = findViewById<Button>(R.id.select_staff_button)
            menuItemListView = findViewById<ListView>(R.id.menu_item_list)
            equipmentListView = findViewById<ListView>(R.id.equipment_list)
            addEventButton = findViewById<Button>(R.id.add_event_button)
            newClientButton = findViewById<Button>(R.id.create_new_client_button)

            findViewById<Button>(R.id.back_to_MainActivity).setOnClickListener { finish() }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
            throw e
        }
    }

    private fun setupSpinners() {
        ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("pending", "confirmed", "completed", "canceled")
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            statusSpinner.adapter = adapter
        }
    }

    private fun setupDateTimePickers() {
        eventDateInput.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { showDatePickerDialog() }
        }

        eventStartTimeInput.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { showTimePickerDialog(this) }
        }

        eventEndTimeInput.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { showTimePickerDialog(this) }
        }
    }

    private fun setupListeners() {
        addEventButton.setOnClickListener {
            if (validateInputs()) addEvent()
        }

        newClientButton.setOnClickListener {
            showAddClientDialog()
        }

        selectStaffButton.setOnClickListener {
            // TODO: Implement staff selection
            showError("Staff selection not yet implemented")
        }
    }

    private fun loadClients() {
        clientCall(
            onSuccess = { clientList ->
                clients = clientList
                updateClientSpinner()
            },
            onFailure = {
                Log.e(TAG, "Failed to load clients", it)
                showError("Failed to load clients")
            }
        )
    }

    private fun updateClientSpinner() {
        ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            clients
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            clientSpinner.adapter = adapter
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                eventDateInput.setText(displayFormatter.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePickerDialog(timeInput: EditText) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute ->
                timeInput.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun validateInputs(): Boolean {
        when {
            eventNameInput.text.isNullOrBlank() -> {
                showError("Event name is required")
                return false
            }
            eventDateInput.text.isNullOrBlank() -> {
                showError("Event date is required")
                return false
            }
            eventStartTimeInput.text.isNullOrBlank() -> {
                showError("Start time is required")
                return false
            }
            eventEndTimeInput.text.isNullOrBlank() -> {
                showError("End time is required")
                return false
            }
            eventLocationInput.text.isNullOrBlank() -> {
                showError("Location is required")
                return false
            }
            expectedGuestsInput.text.isNullOrBlank() -> {
                showError("Number of guests is required")
                return false
            }
            clients.isEmpty() -> {
                showError("Please add a client first")
                return false
            }
        }
        return true
    }

    private fun addEvent() {
        try {
            if (clientSpinner.selectedItem == null) {
                showError("Please select a client")
                return
            }

            // Get users first
            DatabaseApi.retrofitService.getUsers().enqueue(object : Callback<List<User>> {
                override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                    if (response.isSuccessful) {
                        val users = response.body()
                        if (users.isNullOrEmpty()) {
                            showError("No users available")
                            return
                        }

                        // Get the admin user (you can modify this logic based on your needs)
                        val adminUser = users.find { it.role == "caterer" }
                        if (adminUser == null) {
                            showError("No admin user found")
                            return
                        }

                        Log.d(TAG, "Found admin user with ID: ${adminUser.id}")
                        createEventWithEmployee(adminUser.id)
                    } else {
                        showError("Failed to get users")
                    }
                }

                override fun onFailure(call: Call<List<User>>, t: Throwable) {
                    Log.e(TAG, "Failed to get users", t)
                    showError("Network error while getting users")
                }
            })

        } catch (e: Exception) {
            Log.e(TAG, "Error in addEvent", e)
            showError("Error: ${e.localizedMessage}")
        }
    }

    private fun createEventWithEmployee(employeeId: Int) {
        try {
            val client = clientSpinner.selectedItem as Client
            val dateStr = try {
                val date = displayFormatter.parse(eventDateInput.text.toString())
                dateFormatter.format(date!!)
            } catch (e: Exception) {
                showError("Invalid date format")
                return
            }

            // Format times with seconds
            val startTime = "${eventStartTimeInput.text.toString()}:00"
            val endTime = "${eventEndTimeInput.text.toString()}:00"

            Log.d(TAG, "Creating event with employee ID: $employeeId")

            DatabaseApi.retrofitService.addEvent(
                name = eventNameInput.text.toString().trim(),
                eventDate = dateStr,
                startTime = startTime,
                endTime = endTime,
                location = eventLocationInput.text.toString().trim(),
                status = statusSpinner.selectedItem.toString(),
                numberOfGuests = expectedGuestsInput.text.toString().toInt(),
                clientId = client.id,
                employeeId = employeeId,
                additionalInfo = ""
            ).enqueue(object : Callback<EventResponse> {
                override fun onResponse(call: Call<EventResponse>, response: Response<EventResponse>) {
                    if (!response.isSuccessful) {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Server error: $errorBody")
                        showError("Server error: ${response.code()}")
                        return
                    }

                    val eventResponse = response.body()
                    if (eventResponse?.status == true) {
                        showSuccess("Event added successfully")
                        clearInputs()
                    } else {
                        showError(eventResponse?.message ?: "Failed to add event")
                    }
                }

                override fun onFailure(call: Call<EventResponse>, t: Throwable) {
                    Log.e(TAG, "Network error", t)
                    showError("Network error: ${t.localizedMessage}")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error in createEventWithEmployee", e)
            showError("Error: ${e.localizedMessage}")
        }
    }

    private fun showAddClientDialog() {
        val dialogView = layoutInflater.inflate(R.layout.activity_addclient, null)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val firstname = dialogView.findViewById<EditText>(R.id.first_name).text.toString()
                val lastname = dialogView.findViewById<EditText>(R.id.last_name).text.toString()
                val email = dialogView.findViewById<EditText>(R.id.email).text.toString()
                val phone = dialogView.findViewById<EditText>(R.id.phone_number).text.toString()

                if (validateClientInputs(firstname, lastname, email, phone)) {
                    addClient(firstname, lastname, email, phone)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun validateClientInputs(
        firstname: String,
        lastname: String,
        email: String,
        phone: String
    ): Boolean {
        when {
            firstname.isBlank() -> {
                showError("First name is required")
                return false
            }
            lastname.isBlank() -> {
                showError("Last name is required")
                return false
            }
            email.isBlank() -> {
                showError("Email is required")
                return false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showError("Invalid email format")
                return false
            }
            phone.isBlank() -> {
                showError("Phone number is required")
                return false
            }
        }
        return true
    }

    private fun addClient(
        firstname: String,
        lastname: String,
        email: String,
        phone: String
    ) {
        addClient(
            firstname = firstname,
            lastname = lastname,
            email = email,
            phonenumber = phone,
            onSuccess = { response ->
                showSuccess(response.message)
                loadClients()
            },
            onPartialSuccess = { response ->
                showError(response.message)
            },
            onFailure = {
                Log.e(TAG, "Failed to add client", it)
                showError("Failed to add client")
            }
        )
    }

    private fun clearInputs() {
        eventNameInput.text.clear()
        eventDateInput.text.clear()
        eventStartTimeInput.text.clear()
        eventEndTimeInput.text.clear()
        eventLocationInput.text.clear()
        expectedGuestsInput.text.clear()
        statusSpinner.setSelection(0)
        if (clients.isNotEmpty()) {
            clientSpinner.setSelection(0)
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}