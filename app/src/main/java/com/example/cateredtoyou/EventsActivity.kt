package com.example.cateredtoyou

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.cateredtoyou.apifiles.*
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class EventsActivity : AppCompatActivity() {
    // UI Components
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
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: Button

    // Data holders
    private var clients = listOf<Client>()
    private var employees = listOf<User>()
    private lateinit var menuItemsAdapter: InventoryAdapter
    private lateinit var equipmentAdapter: InventoryAdapter

    companion object {
        private const val TAG = "EventsActivity"
        private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        private val displayFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)

        try {
            initializeViews()
            setupSpinners()
            setupDateTimePickers()
            setupAdapters()
            setupListeners()
            loadInitialData()
        } catch (e: Exception) {
            Log.e(TAG, "Error during initialization", e)
            showError("Failed to initialize: ${e.localizedMessage}")
            finish()
        }
    }

    private fun initializeViews() {
        // Initialize all view references
        eventNameInput = findViewById(R.id.event_name_input)
        eventDateInput = findViewById(R.id.event_date_input)
        eventStartTimeInput = findViewById(R.id.event_start_time_input)
        eventEndTimeInput = findViewById(R.id.event_end_time_input)
        eventLocationInput = findViewById(R.id.event_location_input)
        clientSpinner = findViewById(R.id.client_spinner)
        expectedGuestsInput = findViewById(R.id.expected_guests_input)
        statusSpinner = findViewById(R.id.status_spinner)
        addEventButton = findViewById(R.id.add_event_button)
        newClientButton = findViewById(R.id.create_new_client_button)
        selectStaffButton = findViewById(R.id.select_staff_button)
        menuItemListView = findViewById(R.id.menu_item_list)
        equipmentListView = findViewById(R.id.equipment_list)
        progressBar = findViewById(R.id.progress_bar)
        backButton = findViewById(R.id.back_to_MainActivity)
    }

    private fun setupSpinners() {
        // Setup status spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.event_statuses,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            statusSpinner.adapter = adapter
        }

        // Initialize empty client spinner
        updateClientSpinner()
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

    private fun setupAdapters() {
        menuItemsAdapter = InventoryAdapter(
            this,
            mutableListOf()
        ) { _, _ -> updateAddEventButtonState() }
        equipmentAdapter = InventoryAdapter(
            this,
            mutableListOf()
        ) { _, _ -> updateAddEventButtonState() }

        menuItemListView.adapter = menuItemsAdapter
        equipmentListView.adapter = equipmentAdapter
    }

    private fun setupListeners() {
        backButton.setOnClickListener { finish() }

        addEventButton.setOnClickListener {
            if (validateInputs()) {
                showProgressBar()
                addEvent()
            }
        }

        newClientButton.setOnClickListener {
            showAddClientDialog()
        }

        selectStaffButton.setOnClickListener {
            // TODO: Implement staff selection
            Toast.makeText(this, "Staff selection coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadInitialData() {
        showProgressBar()
        loadClients()
        loadEmployees()
        loadInventory()
    }

    private fun loadInventory() {
        Log.d(TAG, "Starting inventory load")
        showProgressBar()

        DatabaseApi.retrofitService.getInventory().enqueue(object : Callback<List<InventoryItem>> {
            override fun onResponse(
                call: Call<List<InventoryItem>>,
                response: Response<List<InventoryItem>>
            ) {
                if (response.isSuccessful) {
                    val items = response.body()
                    if (items != null) {
                        Log.d(TAG, "Successfully loaded ${items.size} inventory items")
                        updateInventoryLists(items)
                    } else {
                        Log.e(TAG, "Inventory response body was null")
                        showError("Failed to load inventory items - empty response")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Failed to load inventory: $errorBody")
                    showError("Server error: ${response.code()} - ${response.message()}")
                }
                hideProgressBar()
            }

            override fun onFailure(call: Call<List<InventoryItem>>, t: Throwable) {
                Log.e(TAG, "Error loading inventory", t)
                showError("Network error while loading inventory: ${t.localizedMessage}")
                hideProgressBar()
            }
        })
    }

    private fun updateInventoryLists(items: List<InventoryItem>) {
        try {
            Log.d(TAG, "Received ${items.size} total inventory items")

            // Food and Beverage items
            val menuItems = items.filter { item ->
                item.category.equals("Food", ignoreCase = true) ||
                        item.category.equals("Beverage", ignoreCase = true)
            }
            Log.d(TAG, "Found ${menuItems.size} menu items: ${menuItems.map { it.itemName }}")

            // Equipment, Utensil, and Decoration items
            val equipmentItems = items.filter { item ->
                item.category.equals("Equipment", ignoreCase = true) ||
                        item.category.equals("Utensil", ignoreCase = true) ||
                        item.category.equals("Decoration", ignoreCase = true)
            }
            Log.d(
                TAG,
                "Found ${equipmentItems.size} equipment items: ${equipmentItems.map { it.itemName }}"
            )

            runOnUiThread {
                menuItemsAdapter.updateItems(menuItems.toMutableList())
                equipmentAdapter.updateItems(equipmentItems.toMutableList())

                // Make sure the ListViews update their display
                (menuItemListView.adapter as? InventoryAdapter)?.notifyDataSetChanged()
                (equipmentListView.adapter as? InventoryAdapter)?.notifyDataSetChanged()

                // Log the adapter counts
                Log.d(TAG, "Menu items adapter count: ${menuItemsAdapter.count}")
                Log.d(TAG, "Equipment adapter count: ${equipmentAdapter.count}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating inventory lists", e)
            showError("Error processing inventory data: ${e.localizedMessage}")
        }
    }

    private fun loadClients() {
        clientCall(
            onSuccess = { clientList ->
                clients = clientList
                updateClientSpinner()
                hideProgressBar()
            },
            onFailure = { error ->
                Log.e(TAG, "Failed to load clients", error)
                showError("Failed to load clients")
                hideProgressBar()
            }
        )
    }

    private fun loadEmployees() {
        DatabaseApi.retrofitService.getUser().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val users = response.body()
                    if (users != null) {
                        employees = users
                        Log.d(TAG, "Successfully loaded ${users.size} employees")
                    } else {
                        Log.e(TAG, "Empty response body for employees")
                        showError("Failed to load employees - empty response")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Failed to load employees: $errorBody")
                    showError("Server error: ${response.code()} - ${response.message()}")
                }
                hideProgressBar()
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e(TAG, "Error loading employees", t)
                showError("Network error while loading employees: ${t.localizedMessage}")
                hideProgressBar()
            }
        })
    }

    private fun updateClientSpinner() {
        ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            clients
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            clientSpinner.adapter = adapter
            // Select the last added client if this update was triggered by adding a new client
            if (clients.isNotEmpty()) {
                selectLatestClient()
            }
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
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                timeInput.setText(timeFormatter.format(calendar.time))
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

            clientSpinner.selectedItem == null -> {
                showError("Please select a client")
                return false
            }

            menuItemsAdapter.getSelectedItems().isEmpty() &&
                    equipmentAdapter.getSelectedItems().isEmpty() -> {
                showError("Please select at least one menu item or equipment")
                return false
            }
        }
        return true
    }

    private fun addEvent() {
        try {
            val clientId = (clientSpinner.selectedItem as? Client)?.id
            val employeeId = employees.firstOrNull()?.id

            if (clientId == null) {
                showError("Invalid client selected")
                hideProgressBar()
                return
            }

            if (employeeId == null) {
                showError("No employee available")
                hideProgressBar()
                return
            }

            val dateStr = try {
                val date = displayFormatter.parse(eventDateInput.text.toString())
                dateFormatter.format(date!!)
            } catch (e: Exception) {
                showError("Invalid date format")
                hideProgressBar()
                return
            }

            val startTime = "${eventStartTimeInput.text}:00"
            val endTime = "${eventEndTimeInput.text}:00"

            DatabaseApi.retrofitService.addEvent(
                name = eventNameInput.text.toString().trim(),
                eventDate = dateStr,
                startTime = startTime,
                endTime = endTime,
                location = eventLocationInput.text.toString().trim(),
                status = statusSpinner.selectedItem.toString(),
                numberOfGuests = expectedGuestsInput.text.toString().toInt(),
                clientId = clientId,
                employeeId = employeeId,
                additionalInfo = "Event created with selections"
            ).enqueue(object : Callback<EventResponse> {
                override fun onResponse(
                    call: Call<EventResponse>,
                    response: Response<EventResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        val eventId = response.body()?.eventId
                        if (eventId != null) {
                            createEventWithInventory(eventId)
                        } else {
                            showError("Failed to get event ID")
                            hideProgressBar()
                        }
                    } else {
                        showError("Failed to create event")
                        hideProgressBar()
                    }
                }

                override fun onFailure(call: Call<EventResponse>, t: Throwable) {
                    Log.e(TAG, "Network error", t)
                    showError("Network error: ${t.localizedMessage}")
                    hideProgressBar()
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error in addEvent", e)
            showError("Error: ${e.localizedMessage}")
            hideProgressBar()
        }
    }


    private fun createEventWithInventory(eventId: Int) {
        val inventoryJson = JSONArray().apply {
            val allItems = menuItemsAdapter.getSelectedItems() + equipmentAdapter.getSelectedItems()
            allItems.forEach { (item, quantity) ->
                put(JSONObject().apply {
                    put("inventory_id", item.id)
                    put("quantity", quantity)
                })
            }
        }.toString()

        DatabaseApi.retrofitService.addEventInventory(eventId, inventoryJson)
            .enqueue(object : Callback<BaseResponse> {
                override fun onResponse(
                    call: Call<BaseResponse>,
                    response: Response<BaseResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        showSuccess("Event created successfully")
                        clearInputs()
                    } else {
                        showError("Failed to save inventory items")
                    }
                    hideProgressBar()
                }

                override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                    Log.e(TAG, "Network error while saving inventory", t)
                    showError("Network error while saving inventory items")
                    hideProgressBar()
                }
            })

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
                    showProgressBar()
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

            !phone.replace(Regex("[()\\s-]"), "").matches(Regex("^\\d{7,15}$")) -> {
                showError("Phone number must contain 7-15 digits")
                return false
            }
        }
        return true
    }

    private fun selectLatestClient() {
        if (clients.isNotEmpty()) {
            val position = clientSpinner.adapter?.count?.minus(1) ?: 0
            clientSpinner.setSelection(position)
        }
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
                // Add a small delay to ensure the spinner is updated before selecting
                android.os.Handler(Looper.getMainLooper()).postDelayed({
                    selectLatestClient()
                }, 300)
            },
            onPartialSuccess = { response ->
                showError(response.message)
                hideProgressBar()
            },
            onFailure = { error ->
                Log.e(TAG, "Failed to add client", error)
                showError("Failed to add client: ${error.localizedMessage}")
                hideProgressBar()
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

        // Reset inventory selections
        menuItemsAdapter.clearSelections()
        equipmentAdapter.clearSelections()

        // Reset client spinner if there are clients
        if (clients.isNotEmpty()) {
            clientSpinner.setSelection(0)
        }

        updateAddEventButtonState()
    }

    private fun updateAddEventButtonState() {
        val hasMenuItems = menuItemsAdapter.getSelectedItems().isNotEmpty()
        val hasEquipment = equipmentAdapter.getSelectedItems().isNotEmpty()
        addEventButton.isEnabled = hasMenuItems || hasEquipment
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
        addEventButton.isEnabled = false
        newClientButton.isEnabled = false
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.GONE
        addEventButton.isEnabled = true
        newClientButton.isEnabled = true
        updateAddEventButtonState()
    }

    private fun showError(message: String) {
        hideProgressBar()
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        hideProgressBar()
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel any pending network requests if needed
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save current input states
        outState.apply {
            putString("eventName", eventNameInput.text.toString())
            putString("eventDate", eventDateInput.text.toString())
            putString("startTime", eventStartTimeInput.text.toString())
            putString("endTime", eventEndTimeInput.text.toString())
            putString("location", eventLocationInput.text.toString())
            putString("guests", expectedGuestsInput.text.toString())
            putInt("statusPosition", statusSpinner.selectedItemPosition)
            putInt("clientPosition", clientSpinner.selectedItemPosition)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Restore saved states
        savedInstanceState.apply {
            eventNameInput.setText(getString("eventName", ""))
            eventDateInput.setText(getString("eventDate", ""))
            eventStartTimeInput.setText(getString("startTime", ""))
            eventEndTimeInput.setText(getString("endTime", ""))
            eventLocationInput.setText(getString("location", ""))
            expectedGuestsInput.setText(getString("guests", ""))

            // Restore spinner selections if they have adapters
            statusSpinner.adapter?.let {
                val statusPos = getInt("statusPosition", 0)
                if (statusPos < it.count) {
                    statusSpinner.setSelection(statusPos)
                }
            }

            clientSpinner.adapter?.let {
                val clientPos = getInt("clientPosition", 0)
                if (clientPos < it.count) {
                    clientSpinner.setSelection(clientPos)
                }
            }
        }
    }
}