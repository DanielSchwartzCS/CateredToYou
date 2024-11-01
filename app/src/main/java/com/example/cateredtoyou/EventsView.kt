package com.example.cateredtoyou

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
        val message = """
            Event: ${event.name}
            Date: ${event.event_date}
            Time: ${event.event_start_time} - ${event.event_end_time}
            Location: ${event.location}
            Guests: ${event.number_of_guests}
            Client: ${event.client.firstname} ${event.client.lastname}
            Status: ${event.status}
        """.trimIndent()

        android.app.AlertDialog.Builder(this)
            .setTitle("Event Details")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
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
            view.findViewById<TextView>(R.id.event_date).text = "Date: ${event.event_date}"
            view.findViewById<TextView>(R.id.event_time).text =
                "Time: ${event.event_start_time} - ${event.event_end_time}"
            view.findViewById<TextView>(R.id.event_location).text = "Location: ${event.location}"
            view.findViewById<TextView>(R.id.event_guests).text =
                "Guests: ${event.number_of_guests}"
            view.findViewById<TextView>(R.id.client_name).text =
                "Client: ${event.client.firstname} ${event.client.lastname}"

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
}