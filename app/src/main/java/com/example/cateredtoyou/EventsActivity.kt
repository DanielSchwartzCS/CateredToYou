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
import androidx.appcompat.app.AppCompatActivity
import com.example.cateredtoyou.apifiles.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

import android.view.animation.AnimationUtils

class EventsActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "EventsActivity"
        private const val MIN_GUESTS = 1
        private const val MAX_GUESTS = 1000
        private const val MIN_EVENT_HOURS = 1
        private const val MAX_EVENT_HOURS = 12
        private const val DEFAULT_EVENT_DURATION_HOURS = 2
        private const val DATE_PICKER_YEARS_RANGE = 2
        private const val ADMIN_USER_ID = 2 // Default admin user ID
        private const val ADMIN_EMPLOYEE_ID = 3  // Default admin employee ID
        private const val STATE_EVENT_NAME = "event_name"
        private const val STATE_EVENT_DATE = "event_date"
        private const val STATE_START_TIME = "start_time"
        private const val STATE_END_TIME = "end_time"
        private const val STATE_LOCATION = "location"
        private const val STATE_GUESTS = "guests"
        private const val STATE_STATUS_POSITION = "status_position"
        private const val STATE_CLIENT_POSITION = "client_position"
        private fun submitEventInventory(eventsActivity: EventsActivity, eventId: Int) {
            val inventoryJson = JSONArray().apply {
                val selectedItems = eventsActivity.menuItemsAdapter.getSelectedItems() + eventsActivity.equipmentAdapter.getSelectedItems()
                selectedItems.forEach { (item, quantity) ->
                    put(JSONObject().apply {
                        put("inventory_id", item.inventory_id)
                        put("quantity", quantity)
                    })
                }
            }.toString()

            Log.d(TAG, "Submitting inventory for event $eventId: $inventoryJson")

            DatabaseApi.retrofitService.addEventInventory(eventId, inventoryJson)
                .enqueue(object : Callback<BaseResponse> {
                    override fun onResponse(
                        call: Call<BaseResponse>,
                        response: Response<BaseResponse>
                    ) {
                        if (response.isSuccessful && response.body()?.status == true) {
                            eventsActivity.showSuccess(eventsActivity.getString(R.string.success))
                            eventsActivity.clearInputs()
                        } else {
                            eventsActivity.handleErrorResponse("Failed to save inventory items", response)
                        }
                        eventsActivity.hideProgressBar()
                    }

                    override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                        eventsActivity.handleNetworkError("Network error while saving inventory", t)
                        eventsActivity.hideProgressBar()
                    }
                })
        }

        private fun createAdditionalInfo(eventsActivity: EventsActivity): String {
            return JSONObject().apply {
                put("menu_items", eventsActivity.menuItemsAdapter.getSelectedItems().size)
                put("equipment_items", eventsActivity.equipmentAdapter.getSelectedItems().size)
                put("created_at", System.currentTimeMillis())
            }.toString()
        }
    }

    // UI Components
    private lateinit var eventNameInput: EditText
    private lateinit var eventDateInput: EditText
    private lateinit var eventStartTimeInput: EditText
    private lateinit var eventEndTimeInput: EditText
    private lateinit var eventLocationInput: EditText
    private lateinit var clientSpinner: Spinner
    private lateinit var expectedGuestsInput: EditText
    private lateinit var additionalInfo: EditText
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
    private var employees = mutableListOf<User>()
    private lateinit var menuItemsAdapter: InventoryAdapter
    private lateinit var equipmentAdapter: InventoryAdapter

    // Date formatters
    private val serverDateFormatter: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val displayDateFormatter: SimpleDateFormat
        get() = SimpleDateFormat("MM/dd/yy", Locale.getDefault())

    private val timeFormatter: SimpleDateFormat
        get() = SimpleDateFormat("HH:mm", Locale.getDefault())

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
            showError(getString(R.string.error_server))
            finish()
        }
    }


    private fun initializeViews() {
        eventNameInput = findViewById(R.id.event_name_input)
        eventDateInput = findViewById(R.id.event_date_input)
        eventStartTimeInput = findViewById(R.id.event_start_time_input)
        eventEndTimeInput = findViewById(R.id.event_end_time_input)
        eventLocationInput = findViewById(R.id.event_location_input)
        clientSpinner = findViewById(R.id.client_spinner)
        expectedGuestsInput = findViewById(R.id.expected_guests_input)
        additionalInfo = findViewById(R.id.notes_input)
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
        ArrayAdapter.createFromResource(
            this,
            R.array.event_statuses,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            statusSpinner.adapter = adapter
        }

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
            setOnClickListener { showTimePickerDialog(this, true) }
        }

        eventEndTimeInput.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { showTimePickerDialog(this, false) }
        }
    }

    private fun setupAdapters() {
        menuItemsAdapter = InventoryAdapter(
            this,
            mutableListOf()
        ) { item, quantity, _ ->  // Added _ for totalCost parameter
            updateAddEventButtonState()
            Log.d("EventsActivity", "Menu item ${item.item_name} quantity changed to $quantity")
        }

        equipmentAdapter = InventoryAdapter(
            this,
            mutableListOf()
        ) { item, quantity, _ ->  // Added _ for totalCost parameter
            updateAddEventButtonState()
            Log.d("EventsActivity", "Equipment ${item.item_name} quantity changed to $quantity")
        }

        menuItemListView.adapter = menuItemsAdapter
        equipmentListView.adapter = equipmentAdapter
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            if (hasUnsavedChanges()) {
                showUnsavedChangesDialog()
            } else {
                finish()
            }
        }

        addEventButton.setOnClickListener {
            if (validateAllInputs()) {
                showConfirmationDialog()
            }
        }

        newClientButton.setOnClickListener {
            showAddClientDialog()

            // Animation for button
            val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_button)  // Scale animation
            val fadeAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_button)   // Fade animation
            val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_button) // Slide animation
            // Apply scale animation on click
            it.startAnimation(scaleAnimation)
            // Or apply fade in and slide in together (for example)
            it.startAnimation(fadeAnimation)
            it.startAnimation(slideAnimation)

        }

        selectStaffButton.setOnClickListener {
            showStaffSelectionDialog()
        }
    }

    private fun loadInitialData() {
        showProgressBar()
        loadClients()
        loadEmployees()
        loadInventory()
    }

    private fun loadClients() {
        DatabaseApi.retrofitService.getClient().enqueue(object : Callback<List<Client>> {
            override fun onResponse(call: Call<List<Client>>, response: Response<List<Client>>) {
                if (response.isSuccessful) {
                    response.body()?.let { clientList ->
                        clients = clientList
                        updateClientSpinner()
                    } ?: run {
                        Log.e(TAG, "Empty response body for clients")
                        showError(getString(R.string.error_server))
                    }
                } else {
                    handleErrorResponse("Failed to load clients", response)
                }
                hideProgressBar()
            }

            override fun onFailure(call: Call<List<Client>>, t: Throwable) {
                handleNetworkError("Failed to load clients", t)
                hideProgressBar()
            }
        })
    }

    private fun loadEmployees() {
        Log.d(TAG, "Loading employees...")
        DatabaseApi.retrofitService.getUser().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    response.body()?.let { userList ->
                        // Debug log the received users
                        Log.d(TAG, "Received users: ${userList.map { "ID: ${it.userId}, Name: ${it.firstName} ${it.lastName}, Role: ${it.role}" }}")

                        employees.clear()
                        employees.addAll(userList)

                        if (employees.isEmpty()) {
                            Log.d(TAG, "No employees found, adding default admin")
                            employees.add(User(
                                userId = ADMIN_USER_ID,
                                username = "admin",
                                firstName = "Admin",
                                lastName = "User",
                                role = "caterer"
                            ))
                        }
                    } ?: run {
                        Log.e(TAG, "Empty response body for employees")
                        addDefaultAdmin()
                    }
                } else {
                    handleErrorResponse("Failed to load employees", response)
                    addDefaultAdmin()
                }
                hideProgressBar()
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e(TAG, "Failed to load employees", t)
                addDefaultAdmin()
                hideProgressBar()
            }
        })
    }

    private fun loadInventory() {
        Log.d(TAG, "Loading inventory")
        showProgressBar()

        DatabaseApi.retrofitService.getInventory().enqueue(object : Callback<List<InventoryItem>> {
            override fun onResponse(
                call: Call<List<InventoryItem>>,
                response: Response<List<InventoryItem>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { items ->
                        Log.d(TAG, "Successfully loaded ${items.size} inventory items")
                        updateInventoryLists(items)
                    } ?: run {
                        Log.e(TAG, "Empty inventory response")
                        showError(getString(R.string.error_server))
                    }
                } else {
                    handleErrorResponse("Failed to load inventory", response)
                }
                hideProgressBar()
            }

            override fun onFailure(call: Call<List<InventoryItem>>, t: Throwable) {
                handleNetworkError("Error loading inventory", t)
                hideProgressBar()
            }
        })
    }

    private fun updateInventoryLists(items: List<InventoryItem>) {
        try {
            val (menuItems, equipmentItems) = items.partition { item ->
                item.category.equals("Food", ignoreCase = true) ||
                        item.category.equals("Beverage", ignoreCase = true)
            }

            runOnUiThread {
                menuItemsAdapter.updateItems(menuItems.toMutableList())
                equipmentAdapter.updateItems(equipmentItems.toMutableList())
                updateAddEventButtonState()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating inventory lists", e)
            showError(getString(R.string.error_server))
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                eventDateInput.setText(displayDateFormatter.format(calendar.time))
                validateDateTime()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
        }.show()
    }

    private fun showTimePickerDialog(timeInput: EditText, isStartTime: Boolean) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                timeInput.setText(timeFormatter.format(calendar.time))

                if (isStartTime) {
                    val endCalendar = calendar.clone() as Calendar
                    endCalendar.add(Calendar.HOUR_OF_DAY, DEFAULT_EVENT_DURATION_HOURS)
                    eventEndTimeInput.setText(timeFormatter.format(endCalendar.time))
                }

                validateDateTime()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun validateDateTime(): Boolean {
        val dateStr = eventDateInput.text.toString()
        val startTimeStr = eventStartTimeInput.text.toString()
        val endTimeStr = eventEndTimeInput.text.toString()

        if (dateStr.isBlank() || startTimeStr.isBlank() || endTimeStr.isBlank()) {
            return false
        }

        return try {
            val eventDateTime = parseDateTime(dateStr, startTimeStr)
            val endDateTime = parseDateTime(dateStr, endTimeStr)

            if (eventDateTime == null || endDateTime == null) {
                showError(getString(R.string.error_date_time_required))
                return false
            }

            if (!isFutureDateTime(eventDateTime)) {
                showError(getString(R.string.error_future_date_required))
                return false
            }

            val durationHours = TimeUnit.MILLISECONDS.toHours(
                endDateTime.timeInMillis - eventDateTime.timeInMillis
            )

            if (durationHours !in MIN_EVENT_HOURS..MAX_EVENT_HOURS) {
                showError(getString(R.string.error_invalid_duration, MIN_EVENT_HOURS, MAX_EVENT_HOURS))
                return false
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error validating date/time", e)
            showError(getString(R.string.error_date_time_required))
            false
        }
    }

    private fun parseDateTime(dateStr: String, timeStr: String): Calendar? {
        return try {
            val date = displayDateFormatter.parse(dateStr) ?: return null
            val time = timeFormatter.parse(timeStr) ?: return null

            Calendar.getInstance().apply {
                this.time = date
                val timeCalendar = Calendar.getInstance().apply {
                    this.time = time
                }
                set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing date/time", e)
            null
        }
    }

    private fun isFutureDateTime(dateTime: Calendar): Boolean {
        val now = Calendar.getInstance()
        return dateTime.after(now)
    }

    private fun validateAllInputs(): Boolean {
        // Remove the employee validation since we have a default
        return when {
            eventNameInput.text.isNullOrBlank() -> {
                showError(getString(R.string.error_event_name_required))
                false
            }
            !validateDateTime() -> false
            eventLocationInput.text.isNullOrBlank() -> {
                showError(getString(R.string.error_location_required))
                false
            }
            !validateGuestCount() -> false
            clientSpinner.selectedItem == null -> {
                showError(getString(R.string.error_client_required))
                false
            }
            !validateInventorySelections() -> false
            else -> true
        }
    }


    private fun validateGuestCount(): Boolean {
        return try {
            val guestCount = expectedGuestsInput.text.toString().toInt()
            when {
                guestCount < MIN_GUESTS -> {
                    showError(getString(R.string.error_min_guests, MIN_GUESTS))
                    false
                }
                guestCount > MAX_GUESTS -> {
                    showError(getString(R.string.error_max_guests, MAX_GUESTS))
                    false
                }
                else -> true
            }
        } catch (e: NumberFormatException) {
            showError(getString(R.string.error_invalid_guest_count))
            false
        }
    }

    private fun validateInventorySelections(): Boolean {
        val menuItems = menuItemsAdapter.getSelectedItems()
        val equipment = equipmentAdapter.getSelectedItems()

        if (menuItems.isEmpty() && equipment.isEmpty()) {
            showError(getString(R.string.error_no_items_selected))
            return false
        }

        val invalidItems = (menuItems + equipment).filter { (item, quantity) ->
            quantity <= 0 || quantity > item.quantity_in_stock
        }

        return if (invalidItems.isNotEmpty()) {
            val itemNames = invalidItems.map { it.key.item_name }.joinToString(", ")
            showError(getString(R.string.error_invalid_quantities, itemNames))
            false
        } else true
    }

    private fun validateClientInputs(
        firstname: String,
        lastname: String,
        email: String,
        phone: String
    ): Boolean {
        return when {
            firstname.isBlank() -> {
                showError(getString(R.string.error_firstname_required))
                false
            }
            lastname.isBlank() -> {
                showError(getString(R.string.error_lastname_required))
                false
            }
            email.isBlank() -> {
                showError(getString(R.string.error_email_required))
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showError(getString(R.string.error_invalid_email))
                false
            }
            phone.isBlank() -> {
                showError(getString(R.string.error_phone_required))
                false
            }
            !phone.replace(Regex("[()\\s-]"), "").matches(Regex("^\\d{7,15}$")) -> {
                showError(getString(R.string.error_invalid_phone))
                false
            }
            else -> true
        }
    }
    private fun addDefaultAdmin() {
        employees.clear()
        employees.add(User(
            userId = ADMIN_USER_ID,
            username = "admin",
            firstName = "Admin",
            lastName = "User",
            role = "caterer"
        ))
    }

    private fun createEvent() {
        if (!validateAllInputs()) return

        showProgressBar()
        val client = clientSpinner.selectedItem as? Client ?: run {
            showError(getString(R.string.error_client_required))
            hideProgressBar()
            return
        }

        try {
            val dateStr = eventDateInput.text.toString()
            val eventDateTime = parseDateTime(dateStr, eventStartTimeInput.text.toString())
                ?: throw IllegalStateException("Invalid date/time")

            val serverDate = serverDateFormatter.format(eventDateTime.time)
            val startTime = "${eventStartTimeInput.text}:00"
            val endTime = "${eventEndTimeInput.text}:00"
            val name = eventNameInput.text.toString().trim()
            val location = eventLocationInput.text.toString().trim()
            val guests = expectedGuestsInput.text.toString().toInt()
            val notes = additionalInfo.text.toString().trim()

            // Debug log
            Log.d(TAG, """
            Creating event with:
            Employee ID: $ADMIN_USER_ID
            Client ID: ${client.client_id}
            Name: $name
            Date: $serverDate
            Time: $startTime - $endTime
            Location: $location
            Guests: $guests
        """.trimIndent())

            DatabaseApi.retrofitService.addEvent(
                name = name,
                eventDate = serverDate,
                startTime = startTime,
                endTime = endTime,
                location = location,
                status = "Planned",
                numberOfGuests = guests,
                clientId = client.client_id,
                employeeId = ADMIN_USER_ID,
                additionalInfo = notes // Empty since we're moving items to event_inventory
            ).enqueue(object : Callback<EventResponse> {
                override fun onResponse(
                    call: Call<EventResponse>,
                    response: Response<EventResponse>
                ) {
                    Log.d(TAG, """
                    Event creation response:
                    URL: ${call.request().url}
                    Response Code: ${response.code()}
                    Response Body: ${response.body()}
                    Raw Response: ${response.raw()}
                """.trimIndent())

                    if (response.isSuccessful) {
                        val eventResponse = response.body()
                        if (eventResponse?.status == true && eventResponse.eventId != null) {
                            submitEventInventory(eventResponse.eventId)
                        } else {
                            Log.e(TAG, "Event creation failed: ${eventResponse?.message}")
                            showError(eventResponse?.message ?: getString(R.string.error_server))
                            hideProgressBar()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Error response: $errorBody")
                        showError(getString(R.string.error_server))
                        hideProgressBar()
                    }
                }

                override fun onFailure(call: Call<EventResponse>, t: Throwable) {
                    Log.e(TAG, "Network error during event creation", t)
                    showError(getString(R.string.error_network))
                    hideProgressBar()
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error creating event", e)
            showError(getString(R.string.error_server))
            hideProgressBar()
        }
    }

    private fun handleEventCreationResponse(response: Response<EventResponse>) {
        if (response.isSuccessful && response.body()?.status == true) {
            response.body()?.eventId?.let { eventId ->
                submitEventInventory(eventId)
            } ?: run {
                showError(getString(R.string.error_server))
                hideProgressBar()
            }
        } else {
            handleErrorResponse("Failed to create event", response)
            hideProgressBar()
        }
    }

    private fun submitEventInventory(eventId: Int) {
        val menuItems = menuItemsAdapter.getSelectedItems()
        val equipmentItems = equipmentAdapter.getSelectedItems()

        val inventoryJson = JSONArray().apply {
            (menuItems + equipmentItems).forEach { (item, quantity) ->
                put(JSONObject().apply {
                    put("inventory_id", item.inventory_id)
                    put("quantity", quantity)
                    put("special_instructions", "")
                })
            }
        }.toString()

        Log.d(TAG, "Submitting inventory for event $eventId: $inventoryJson")

        DatabaseApi.retrofitService.addEventInventory(
            eventId = eventId,
            inventoryItems = inventoryJson
        ).enqueue(object : Callback<BaseResponse> {
            override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    showSuccess(getString(R.string.success))
                    clearInputs()
                } else {
                    handleErrorResponse("Failed to save inventory items", response)
                }
                hideProgressBar()
            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                handleNetworkError("Network error while saving inventory", t)
                hideProgressBar()
            }
        })
    }

    fun addClient(
        firstname: String,
        lastname: String,
        email: String,
        phonenumber: String,
        billing: String,
        contactMethod: String,
        notes: String,
        onSuccess: (AddClientResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        // Input validation
        if (firstname.isBlank() || lastname.isBlank() ||
            email.isBlank() || phonenumber.isBlank() || billing.isBlank()
            || contactMethod.isBlank() || notes.isBlank()) {
            onError("All fields must not be empty")
            return
        }

        // Sanitize inputs
        val sanitizedPhone = phonenumber.filter { it.isDigit() }
        if (sanitizedPhone.length < 7 || sanitizedPhone.length > 15) {
            onError("Invalid phone number format")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onError("Invalid email format")
            return
        }

        DatabaseApi.retrofitService.addClient(
            firstname = firstname.trim(),
            lastname = lastname.trim(),
            email = email.trim(),
            phonenumber = sanitizedPhone,
            billing = billing.trim(),
            contactMethod = contactMethod.trim(),
            notes = notes.trim()
        ).enqueue(object : Callback<AddClientResponse> {
            override fun onResponse(
                call: Call<AddClientResponse>,
                response: Response<AddClientResponse>
            ) {
                when {
                    response.isSuccessful && response.body()?.status == true -> {
                        onSuccess(response.body()!!)
                    }
                    response.code() == 500 -> {
                        // Handle 500 Internal Server Error specifically
                        Log.e("AddClient", "Server error: ${response.errorBody()?.string()}")
                        onError("Server error occurred. Please try again later.")
                    }
                    else -> {
                        // Handle other error cases
                        val errorMsg = try {
                            response.errorBody()?.string() ?: "Unknown error occurred"
                        } catch (e: Exception) {
                            "Error processing server response"
                        }
                        Log.e("AddClient", "Error response: $errorMsg")
                        onError(errorMsg)
                    }
                }
            }

            override fun onFailure(call: Call<AddClientResponse>, t: Throwable) {
                Log.e("AddClient", "Network error", t)
                onError("Network error: ${t.message ?: "Unknown error occurred"}")
            }
        })
    }



    private fun showAddClientDialog() {
        val dialogView = layoutInflater.inflate(R.layout.activity_addclient, null)

        val contactMethods = listOf("Email", "Text", "Phone")
        val spinner = dialogView.findViewById<Spinner>(R.id.preferred_contact_method)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            contactMethods
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter


        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
//            .setTitle(R.string.add_client_title)
            .setPositiveButton(R.string.add) { dialog, _ ->
                val firstname = dialogView.findViewById<EditText>(R.id.first_name).text.toString()
                val lastname = dialogView.findViewById<EditText>(R.id.last_name).text.toString()
                val email = dialogView.findViewById<EditText>(R.id.email).text.toString()
                val phone = dialogView.findViewById<EditText>(R.id.phone_number).text.toString()
                val billing = dialogView.findViewById<EditText>(R.id.billing_address).text.toString()
                val notes = dialogView.findViewById<EditText>(R.id.notes).text.toString()
                val contact = spinner.selectedItem.toString()
                if (validateClientInputs(firstname, lastname, email, phone)) {
                    showProgressBar()
                    addClient(
                        firstname = firstname,
                        lastname = lastname,
                        email = email,
                        phonenumber = phone,
                        billing = billing,
                        contactMethod = contact,
                        notes = notes,
                        onSuccess = { response ->
                            hideProgressBar()
                            showSuccess(response.message)
                            loadClients()
                            Handler(Looper.getMainLooper()).postDelayed({
                                selectLatestClient()
                            }, 300)
                        },
                        onError = { errorMessage ->
                            hideProgressBar()
                            showError(errorMessage)
                        }
                    )
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }


    private fun showStaffSelectionDialog() {
        // For now, just show that the admin user is assigned
        Toast.makeText(this, "Event will be assigned to admin", Toast.LENGTH_SHORT).show()
    }

    private fun updateStaffButtonText(selectedCount: Int) {
        selectStaffButton.text = getString(R.string.select_staff_format, selectedCount)
    }

    private fun showConfirmationDialog() {
        val client = clientSpinner.selectedItem as Client
        val summary = getString(
            R.string.event_confirmation_summary,
            additionalInfo.text,
            eventNameInput.text,
            eventDateInput.text,
            "${eventStartTimeInput.text} - ${eventEndTimeInput.text}",
            eventLocationInput.text,
            "${client.first_name} ${client.last_name}",
            expectedGuestsInput.text,
            menuItemsAdapter.getSelectedItems().size,
            equipmentAdapter.getSelectedItems().size
        )

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.confirm_event_creation)
            .setMessage(summary)
            .setPositiveButton(R.string.create) { _, _ -> createEvent() }
            .setNegativeButton(R.string.edit, null)
            .show()
    }

    private fun showUnsavedChangesDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.unsaved_changes)
            .setMessage(R.string.unsaved_changes_message)
            .setPositiveButton(R.string.leave) { _, _ -> finish() }
            .setNegativeButton(R.string.stay, null)
            .show()
    }

    private fun updateClientSpinner() {
        ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            clients
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            clientSpinner.adapter = adapter
            if (clients.isNotEmpty()) {
                selectLatestClient()
            }
        }
    }

    private fun selectLatestClient() {
        val position = clientSpinner.adapter?.count?.minus(1) ?: 0
        clientSpinner.setSelection(position)
    }

    private fun createInventoryJson(selectedItems: Map<InventoryItem, Int>): String {
        return JSONArray().apply {
            selectedItems.forEach { (item, quantity) ->
                put(JSONObject().apply {
                    put("inventory_id", item.inventory_id)
                    put("quantity", quantity)
                })
            }
        }.toString()
    }

    private fun createAdditionalInfo(): String {
        return JSONObject().apply {
            put("menu_items", menuItemsAdapter.getSelectedItems().size)
            put("equipment_items", equipmentAdapter.getSelectedItems().size)
            put("created_at", System.currentTimeMillis())
        }.toString()
    }

    private fun clearInputs() {
        eventNameInput.text.clear()
        eventDateInput.text.clear()
        eventStartTimeInput.text.clear()
        eventEndTimeInput.text.clear()
        eventLocationInput.text.clear()
        expectedGuestsInput.text.clear()
        statusSpinner.setSelection(0)
        menuItemsAdapter.clearSelections()
        equipmentAdapter.clearSelections()
        if (clients.isNotEmpty()) {
            clientSpinner.setSelection(0)
        }
        updateAddEventButtonState()
    }

    private fun hasUnsavedChanges(): Boolean {
        return eventNameInput.text.isNotEmpty() ||
                eventDateInput.text.isNotEmpty() ||
                eventStartTimeInput.text.isNotEmpty() ||
                eventEndTimeInput.text.isNotEmpty() ||
                eventLocationInput.text.isNotEmpty() ||
                expectedGuestsInput.text.isNotEmpty() ||
                menuItemsAdapter.getSelectedItems().isNotEmpty() ||
                equipmentAdapter.getSelectedItems().isNotEmpty()
    }

    private fun handleErrorResponse(message: String, response: Response<*>) {
        val errorBody = response.errorBody()?.string()
        Log.e(TAG, "$message: $errorBody")
        showError(getString(R.string.error_server))
    }

    private fun handleNetworkError(message: String, t: Throwable) {
        Log.e(TAG, message, t)
        showError(getString(R.string.error_network))
    }

    private fun showError(message: String) {
        hideProgressBar()
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        hideProgressBar()
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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

    private fun updateAddEventButtonState() {
        addEventButton.isEnabled = menuItemsAdapter.getSelectedItems().isNotEmpty() ||
                equipmentAdapter.getSelectedItems().isNotEmpty()
    }

    data class EventRequest(
        val name: String,
        val eventDate: String,
        val startTime: String,
        val endTime: String,
        val location: String,
        val status: String,
        val numberOfGuests: Int,
        val clientId: Int,
        val employeeId: Int,
        val additionalInfo: String
    ) {
        override fun toString(): String {
            return "EventRequest(name='$name', date='$eventDate', start='$startTime', " +
                    "end='$endTime', location='$location', status='$status', " +
                    "guests=$numberOfGuests, clientId=$clientId, employeeId=$employeeId)"
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putString(STATE_EVENT_NAME, eventNameInput.text.toString())
            putString(STATE_EVENT_DATE, eventDateInput.text.toString())
            putString(STATE_START_TIME, eventStartTimeInput.text.toString())
            putString(STATE_END_TIME, eventEndTimeInput.text.toString())
            putString(STATE_LOCATION, eventLocationInput.text.toString())
            putString(STATE_GUESTS, expectedGuestsInput.text.toString())
            putInt(STATE_STATUS_POSITION, statusSpinner.selectedItemPosition)
            putInt(STATE_CLIENT_POSITION, clientSpinner.selectedItemPosition)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.apply {
            eventNameInput.setText(getString(STATE_EVENT_NAME, ""))
            eventDateInput.setText(getString(STATE_EVENT_DATE, ""))
            eventStartTimeInput.setText(getString(STATE_START_TIME, ""))
            eventEndTimeInput.setText(getString(STATE_END_TIME, ""))
            eventLocationInput.setText(getString(STATE_LOCATION, ""))
            expectedGuestsInput.setText(getString(STATE_GUESTS, ""))

            statusSpinner.adapter?.let {
                val statusPos = getInt(STATE_STATUS_POSITION, 0)
                if (statusPos < it.count) {
                    statusSpinner.setSelection(statusPos)
                }
            }

            clientSpinner.adapter?.let {
                val clientPos = getInt(STATE_CLIENT_POSITION, 0)
                if (clientPos < it.count) {
                    clientSpinner.setSelection(clientPos)
                }
            }
        }
    }
}