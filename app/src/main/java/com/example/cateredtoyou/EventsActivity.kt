package com.example.cateredtoyou

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.cateredtoyou.apifiles.DatabaseApi
import com.example.cateredtoyou.apifiles.User
import com.example.cateredtoyou.apifiles.clientCall
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
    private lateinit var selectStaffButton: Button
    private lateinit var menuItemListView: ListView
    private lateinit var equipmentListView: ListView
    private lateinit var addEventButton: Button
    private lateinit var eventsList: ListView

    private lateinit var events: ArrayList<Event>
    private lateinit var adapter: ArrayAdapter<Event>

    private lateinit var clients: List<Client>
    private lateinit var staffList: List<Staff>
    private lateinit var menuItems: List<MenuItem>
    private lateinit var equipmentList: List<Equipment>

    private val selectedStaff = mutableListOf<Staff>()
    private val selectedMenuItems = mutableListOf<MenuItem>()
    private val selectedEquipment = mutableListOf<Equipment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)

        clients = emptyList()

        initializeViews()
        setupDummyData()
        setupListViews()
        setupEventsList()
        setupListeners()
    }

    private fun initializeViews() {
        eventNameInput = findViewById(R.id.event_name_input)
        eventDateInput = findViewById(R.id.event_date_input)
        eventStartTimeInput = findViewById(R.id.event_start_time_input)
        eventEndTimeInput = findViewById(R.id.event_end_time_input)
        eventLocationInput = findViewById(R.id.event_location_input)
        clientSpinner = findViewById(R.id.client_spinner)
        expectedGuestsInput = findViewById(R.id.expected_guests_input)
        statusSpinner = findViewById(R.id.status_spinner)
        selectStaffButton = findViewById(R.id.select_staff_button)
        menuItemListView = findViewById(R.id.menu_item_list)
        equipmentListView = findViewById(R.id.equipment_list)
        addEventButton = findViewById(R.id.add_event_button)
        eventsList = findViewById(R.id.events_list)

        val backButton: Button = findViewById(R.id.back_to_MainActivity)
        backButton.setOnClickListener { finish() }
    }

    private fun setupDummyData() {
        clientCall(onSuccess = {response -> clients = response; setupSpinners()},
            onFailure = { Log.e("EventsActivity","Failed to connect");
                Toast.makeText(this, "Couldn't connect to client list", Toast.LENGTH_SHORT).show()})

//        clients = listOf(
//            Client(1, "John Doe", "123-456-7890", "john@example.com", "123 Main St"),
//            Client(2, "Jane Smith", "987-654-3210", "jane@example.com", "456 Elm St")
//        )
        staffList = listOf(
            Staff(1, "Alice", "Chef"),
            Staff(2, "Bob", "Waiter"),
            Staff(3, "Charlie", "Bartender")
        )
        menuItems = listOf(
            MenuItem(1, "Chicken Parmesan", "Breaded chicken with marinara sauce", 15.99, "Main Course"),
            MenuItem(2, "Caesar Salad", "Romaine lettuce with caesar dressing", 8.99, "Appetizer")
        )
        equipmentList = listOf(
            Equipment(1, "Tables", 10),
            Equipment(2, "Chairs", 50),
            Equipment(3, "Plates", 100)
        )
    }

    private fun setupSpinners() {
        ArrayAdapter(this, android.R.layout.simple_spinner_item, clients).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            clientSpinner.adapter = adapter
        }

        ArrayAdapter(this, android.R.layout.simple_spinner_item, Event.EventStatus.values()).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            statusSpinner.adapter = adapter
        }
    }

    private fun setupListViews() {
        val menuItemAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, menuItems)
        menuItemListView.adapter = menuItemAdapter
        menuItemListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        val equipmentAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, equipmentList)
        equipmentListView.adapter = equipmentAdapter
        equipmentListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
    }

    private fun setupEventsList() {
        events = ArrayList()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, events)
        eventsList.adapter = adapter
    }

    private fun setupListeners() {
        addEventButton.setOnClickListener {
            if (validateInputs()) {
                addEvent()
            }
        }

        selectStaffButton.setOnClickListener {
            showStaffSelectionDialog()
        }

        eventsList.setOnItemLongClickListener { _, _, position, _ ->
            showEventOptions(position)
            true
        }
    }

    private fun validateInputs(): Boolean {
        // Implement input validation logic
        return true
    }

    private fun addEvent() {
        val name = eventNameInput.text.toString()
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(eventDateInput.text.toString())!!
        val startTime = eventStartTimeInput.text.toString()
        val endTime = eventEndTimeInput.text.toString()
        val location = eventLocationInput.text.toString()
        val status = statusSpinner.selectedItem as Event.EventStatus
        val client = clientSpinner.selectedItem as Client
        val expectedGuests = expectedGuestsInput.text.toString().toInt()

        val newEvent = Event(
            name = name,
            date = date,
            startTime = startTime,
            endTime = endTime,
            location = location,
            status = status,
            client = client,
            expectedGuests = expectedGuests,
            menu = selectedMenuItems,
            staffAssigned = selectedStaff,
            equipmentNeeded = selectedEquipment
        )

        events.add(newEvent)
        adapter.notifyDataSetChanged()
        clearInputs()
        Toast.makeText(this, "Event added", Toast.LENGTH_SHORT).show()
    }

    private fun showStaffSelectionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Staff")

        val staffItems = staffList.map { it.toString() }.toTypedArray()
        val checkedItems = BooleanArray(staffList.size) { selectedStaff.contains(staffList[it]) }

        builder.setMultiChoiceItems(staffItems, checkedItems) { _, which, isChecked ->
            if (isChecked) {
                selectedStaff.add(staffList[which])
            } else {
                selectedStaff.remove(staffList[which])
            }
        }

        builder.setPositiveButton("OK") { _, _ ->
            updateStaffSelectionButton()
        }

        builder.setNegativeButton("Cancel", null)

        val dialog = builder.create()
        dialog.show()
    }

    private fun updateStaffSelectionButton() {
        selectStaffButton.text = "Selected Staff: ${selectedStaff.size}"
    }

    private fun clearInputs() {
        eventNameInput.text.clear()
        eventDateInput.text.clear()
        eventStartTimeInput.text.clear()
        eventEndTimeInput.text.clear()
        eventLocationInput.text.clear()
        expectedGuestsInput.text.clear()
        clientSpinner.setSelection(0)
        statusSpinner.setSelection(0)
        selectedStaff.clear()
        updateStaffSelectionButton()
        selectedMenuItems.clear()
        selectedEquipment.clear()
        for (i in 0 until menuItemListView.count) {
            menuItemListView.setItemChecked(i, false)
        }
        for (i in 0 until equipmentListView.count) {
            equipmentListView.setItemChecked(i, false)
        }
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
        adapter.notifyDataSetChanged()
        Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show()
    }
}