package com.example.cateredtoyou

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class EventsActivity_Core : AppCompatActivity() {

    companion object {
        private const val TAG = "EventsActivity"
        private const val MIN_GUESTS = 1
        private const val MAX_GUESTS = 1000
        private const val MIN_EVENT_HOURS = 1
        private const val MAX_EVENT_HOURS = 12
        private const val DEFAULT_EVENT_DURATION_HOURS = 2
        private const val ADMIN_USER_ID = 1
    }

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
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: Button

    // Adapters and Data Holders
    private lateinit var menuItemsAdapter: InventoryAdapter
    private lateinit var equipmentAdapter: InventoryAdapter
    private var clients = listOf<Client>()

    // Date Formatters
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
        } catch (e: Exception) {
            Log.e(TAG, "Error during initialization", e)
            showError(getString(R.string.error_server))
            finish()
        }
    }

    private fun initializeViews() {
        // View initialization logic from original implementation
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
        ) { item, quantity, _ ->
            updateAddEventButtonState()
        }

        equipmentAdapter = InventoryAdapter(
            this,
            mutableListOf()
        ) { item, quantity, _ ->
            updateAddEventButtonState()
        }
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            if (hasUnsavedChanges()) {
                showUnsavedChangesDialog()
            } else {
                finish()
            }
        }

        newClientButton.setOnClickListener {
            val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_button)
            it.startAnimation(scaleAnimation)
        }

        addEventButton.setOnClickListener {
            if (validateAllInputs()) {
                showConfirmationDialog()
            }
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

    fun validateAllInputs(): Boolean {
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
            quantity <= 0 || quantity > item.quantity
        }

        return if (invalidItems.isNotEmpty()) {
            val itemNames = invalidItems.map { it.key.itemName }.joinToString(", ")
            showError(getString(R.string.error_invalid_quantities, itemNames))
            false
        } else true
    }

    private fun showConfirmationDialog() {
        val client = clientSpinner.selectedItem as Client
        val summary = getString(
            R.string.event_confirmation_summary,
            eventNameInput.text,
            eventDateInput.text,
            "${eventStartTimeInput.text} - ${eventEndTimeInput.text}",
            eventLocationInput.text,
            "${client.firstname} ${client.lastname}",
            expectedGuestsInput.text,
            menuItemsAdapter.getSelectedItems().size,
            equipmentAdapter.getSelectedItems().size
        )

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.confirm_event_creation)
            .setMessage(summary)
            .setPositiveButton(R.string.create) { _, _ ->
                // TODO: Implement event creation logic
            }
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

    private fun updateAddEventButtonState() {
        addEventButton.isEnabled = menuItemsAdapter.getSelectedItems().isNotEmpty() ||
                equipmentAdapter.getSelectedItems().isNotEmpty()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // State preservation logic
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // State restoration logic
    }
}