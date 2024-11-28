package com.example.cateredtoyou

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.cateredtoyou.apifiles.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class EventsView : AppCompatActivity() {
    private lateinit var eventsListView: ListView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private var events = mutableListOf<EventData>()

    companion object {
        private const val TAG = "EventsView"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eventsview)

        initializeViews()
        setupListeners()
        loadEvents()
    }

    private fun initializeViews() {
        eventsListView = findViewById(R.id.events_list_view)
        progressBar = findViewById(R.id.progress_bar)
        emptyView = findViewById(R.id.empty_view)
        swipeRefresh = findViewById(R.id.swipeRefresh)

        findViewById<ImageButton>(R.id.back_to_MainActivity).setOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        eventsListView.setOnItemClickListener { _, _, position, _ ->
            val event = events[position]
            showEventDetails(event)
        }

        swipeRefresh.setOnRefreshListener {
            loadEvents()
        }
    }

    private fun loadEvents() {
        showLoading(true)
        Log.d(TAG, "Loading events...")

        DatabaseApi.retrofitService.getEvents().enqueue(object : Callback<EventsResponse> {
            override fun onResponse(call: Call<EventsResponse>, response: Response<EventsResponse>) {
                Log.d(TAG, "Response received: ${response.code()}")
                showLoading(false)

                if (response.isSuccessful) {
                    val eventsResponse = response.body()
                    if (eventsResponse?.status == true) {
                        updateEventsList(eventsResponse.events ?: emptyList())
                    } else {
                        showError(eventsResponse?.message ?: "Failed to load events")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Server error: $errorBody")
                    showError("Server error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<EventsResponse>, t: Throwable) {
                Log.e(TAG, "Network error", t)
                showLoading(false)
                showError("Network error: ${t.localizedMessage}")
            }
        })
    }

    private fun updateEventsList(newEvents: List<EventData>) {
        Log.d(TAG, "Updating events list with ${newEvents.size} events")

        events.clear()
        events.addAll(newEvents)

        if (events.isEmpty()) {
            showEmptyState()
        } else {
            eventsListView.isVisible = true
            emptyView.isVisible = false
            eventsListView.adapter = EventAdapter(this, events)
        }
    }

    private fun showEventDetails(event: EventData) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_event_details, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Set up basic info
        val basicInfo = """
        Event: ${event.name}
        Date: ${event.eventDate}
        Time: ${event.eventStartTime} - ${event.eventEndTime}
        Location: ${event.location}
        Guests: ${event.numberOfGuests}
        Client: ${event.client.first_name} ${event.client.last_name}
        Status: ${event.status}
    """.trimIndent()

        dialogView.findViewById<TextView>(R.id.event_basic_info).text = basicInfo

        // Set up delete button
        dialogView.findViewById<Button>(R.id.delete_button).setOnClickListener {
            dialog.dismiss()
            showDeleteConfirmation(event)
        }

        // Load event inventory items with progress indicator
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.inventory_progress)
        progressBar.visibility = View.VISIBLE

        loadEventInventory(event.id, dialogView)

        // Set up close button
        dialogView.findViewById<Button>(R.id.close_button).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun showDeleteConfirmation(event: EventData) {
        AlertDialog.Builder(this)
            .setTitle("Delete Event")
            .setMessage("Are you sure you want to delete '${event.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                deleteEvent(event)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun deleteEvent(event: EventData) {
        showLoading(true)

        DatabaseApi.retrofitService.deleteEvent(event.id).enqueue(object : Callback<DeleteResponse> {
            override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {
                showLoading(false)
                if (response.isSuccessful && response.body()?.status == true) {
                    // Remove from list and update UI
                    events.remove(event)
                    if (events.isEmpty()) {
                        showEmptyState()
                    } else {
                        eventsListView.adapter = EventAdapter(this@EventsView, events)
                    }
                    Toast.makeText(this@EventsView,
                        response.body()?.message ?: "Event deleted successfully",
                        Toast.LENGTH_SHORT).show()
                } else {
                    showError(response.body()?.message ?: "Failed to delete event")
                }
            }

            override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                showLoading(false)
                showError("Network error while deleting event: ${t.localizedMessage}")
            }
        })
    }

    private fun loadEventInventory(eventId: Int, dialogView: View) {
        // Show progress indicator
        dialogView.findViewById<ProgressBar>(R.id.inventory_progress)?.visibility = View.VISIBLE

        DatabaseApi.retrofitService.getEventInventory(eventId).enqueue(object : Callback<EventInventoryResponse> {
            override fun onResponse(
                call: Call<EventInventoryResponse>,
                response: Response<EventInventoryResponse>
            ) {
                dialogView.findViewById<ProgressBar>(R.id.inventory_progress)?.visibility = View.GONE

                if (response.isSuccessful && response.body()?.items != null) {
                    updateEventInventoryLists(response.body()!!, dialogView)
                } else {
                    // Handle error case
                    val errorMessage = response.body()?.message ?: "Failed to load event inventory"
                    showError(errorMessage)

                    // Show empty state in lists
                    val menuListView = dialogView.findViewById<ListView>(R.id.event_menu_items)
                    val equipmentListView = dialogView.findViewById<ListView>(R.id.event_equipment_items)

                    menuListView.adapter = EventInventoryAdapter(this@EventsView, emptyList())
                    equipmentListView.adapter = EventInventoryAdapter(this@EventsView, emptyList())
                }
            }

            override fun onFailure(call: Call<EventInventoryResponse>, t: Throwable) {
                dialogView.findViewById<ProgressBar>(R.id.inventory_progress)?.visibility = View.GONE
                showError("Error loading event inventory: ${t.localizedMessage}")

                // Show empty state in lists
                val menuListView = dialogView.findViewById<ListView>(R.id.event_menu_items)
                val equipmentListView = dialogView.findViewById<ListView>(R.id.event_equipment_items)

                menuListView.adapter = EventInventoryAdapter(this@EventsView, emptyList())
                equipmentListView.adapter = EventInventoryAdapter(this@EventsView, emptyList())
            }
        })
    }
    private fun updateEventInventoryLists(
        inventory: EventInventoryResponse,
        dialogView: View
    ) {
        try {
            // Safely handle potentially null items list
            val items = inventory.items ?: emptyList()

            // Split items into menu and equipment
            val menuItems = items.filter {
                it.category?.equals("Food", ignoreCase = true) == true ||
                        it.category?.equals("Beverage", ignoreCase = true) == true
            }
            val equipmentItems = items.filter {
                it.category in listOf("Equipment", "Utensil", "Decoration")
            }

            // Update menu items list
            val menuListView = dialogView.findViewById<ListView>(R.id.event_menu_items)
            menuListView.adapter = EventInventoryAdapter(this, menuItems)

            // Update equipment list
            val equipmentListView = dialogView.findViewById<ListView>(R.id.event_equipment_items)
            equipmentListView.adapter = EventInventoryAdapter(this, equipmentItems)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating inventory lists", e)
            showError("Error displaying inventory items")

            // Show empty state in lists
            val menuListView = dialogView.findViewById<ListView>(R.id.event_menu_items)
            val equipmentListView = dialogView.findViewById<ListView>(R.id.event_equipment_items)

            menuListView.adapter = EventInventoryAdapter(this, emptyList())
            equipmentListView.adapter = EventInventoryAdapter(this, emptyList())
        }
    }



    private fun showLoading(show: Boolean) {
        progressBar.isVisible = show
        eventsListView.isVisible = !show
        swipeRefresh.isRefreshing = show
    }

    private fun showError(message: String) {
        Log.e(TAG, "Error: $message")
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showEmptyState() {
        emptyView.isVisible = true
        eventsListView.isVisible = false
    }

    private inner class EventAdapter(
        context: Context,
        private val events: List<EventData>
    ) : ArrayAdapter<EventData>(context, 0, events) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.event_item, parent, false)

            val event = getItem(position)
            if (event != null) {
                bindEventData(view, event)
                setupEventItemBackground(view, position)
            }

            return view
        }

        private fun bindEventData(view: View, event: EventData) {
            view.findViewById<TextView>(R.id.event_name).text = event.name
            view.findViewById<TextView>(R.id.event_date).text = "Date: ${event.eventDate}"
            view.findViewById<TextView>(R.id.event_time).text =
                "Time: ${event.eventStartTime} - ${event.eventEndTime}"
            view.findViewById<TextView>(R.id.event_location).text = "Location: ${event.location}"
            view.findViewById<TextView>(R.id.event_guests).text =
                "Guests: ${event.numberOfGuests}"
            view.findViewById<TextView>(R.id.client_name).text =
                "Client: ${event.client.first_name} ${event.client.last_name}"

            val statusView = view.findViewById<TextView>(R.id.event_status)
            statusView.text = event.status.uppercase()
            statusView.setBackgroundResource(getStatusBackground(event.status))
            statusView.setTextColor(ContextCompat.getColor(context, android.R.color.white))
        }

        private fun setupEventItemBackground(view: View, position: Int) {
            view.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    if (position % 2 == 0) android.R.color.white else android.R.color.darker_gray
                )
            )
        }

        private fun getStatusBackground(status: String): Int {
            return when (status.lowercase()) {
                "pending" -> R.drawable.status_pending_background
                "confirmed" -> R.drawable.status_confirmed_background
                "completed" -> R.drawable.status_completed_background
                "canceled" -> R.drawable.status_canceled_background
                else -> R.drawable.status_pending_background
            }
        }
    }
    private inner class EventInventoryAdapter(
        context: Context,
        private val items: List<EventInventoryItem>
    ) : ArrayAdapter<EventInventoryItem>(context, android.R.layout.simple_list_item_2, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(
                android.R.layout.simple_list_item_2,
                parent,
                false
            )

            val item = getItem(position)!!
            view.findViewById<TextView>(android.R.id.text1).text = item.itemName
            view.findViewById<TextView>(android.R.id.text2).text =
                "Quantity: ${item.quantity} ${item.unitOfMeasurement ?: "units"}"

            return view
        }
    }

}