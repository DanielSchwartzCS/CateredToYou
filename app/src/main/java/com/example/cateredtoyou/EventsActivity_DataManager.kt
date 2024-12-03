package com.example.cateredtoyou

import android.util.Log
import android.util.Patterns
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data Management class for EventsActivity
 * Handles network operations, data loading, and complex data processing
 */
internal class EventsActivity_DataManager(private val activity: EventsActivity) {

    companion object {
        private const val TAG = "EventsActivity_DataManager"
        private const val ADMIN_USER_ID = 1 // Default admin user ID
    }

    /**
     * Load initial data for the events activity
     * Sequentially loads clients, employees, and inventory
     */
    internal fun loadInitialData() {
        activity.showProgressBar()
        loadClients()
        loadEmployees()
        loadInventory()
    }

    /**
     * Load clients from the database
     * Updates the client spinner upon successful retrieval
     */
    internal fun loadClients() {
        DatabaseApi.retrofitService.getClient().enqueue(object : Callback<List<Client>> {
            override fun onResponse(call: Call<List<Client>>, response: Response<List<Client>>) {
                if (response.isSuccessful) {
                    response.body()?.let { clientList ->
                        activity.clients = clientList
                        activity.updateClientSpinner()
                    } ?: run {
                        Log.e(TAG, "Empty response body for clients")
                        activity.showError(activity.getString(R.string.error_server))
                    }
                } else {
                    handleErrorResponse("Failed to load clients", response)
                }
                activity.hideProgressBar()
            }

            override fun onFailure(call: Call<List<Client>>, t: Throwable) {
                handleNetworkError("Failed to load clients", t)
                activity.hideProgressBar()
            }
        })
    }

    /**
     * Load employees from the database
     * Adds a default admin user if no employees are retrieved
     */
    internal fun loadEmployees() {
        Log.d(TAG, "Loading employees...")
        DatabaseApi.retrofitService.getUser().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    response.body()?.let { userList ->
                        Log.d(TAG, "Received users: ${userList.map { "ID: ${it.userId}, Name: ${it.firstName} ${it.lastName}, Role: ${it.role}" }}")

                        activity.employees.clear()
                        activity.employees.addAll(userList)

                        if (activity.employees.isEmpty()) {
                            Log.d(TAG, "No employees found, adding default admin")
                            addDefaultAdmin()
                        }
                    } ?: run {
                        Log.e(TAG, "Empty response body for employees")
                        addDefaultAdmin()
                    }
                } else {
                    handleErrorResponse("Failed to load employees", response)
                    addDefaultAdmin()
                }
                activity.hideProgressBar()
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e(TAG, "Failed to load employees", t)
                addDefaultAdmin()
                activity.hideProgressBar()
            }
        })
    }

    /**
     * Load inventory items from the database
     */
    internal fun loadInventory() {
        Log.d(TAG, "Loading inventory")
        activity.showProgressBar()

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
                        activity.showError(activity.getString(R.string.error_server))
                    }
                } else {
                    handleErrorResponse("Failed to load inventory", response)
                }
                activity.hideProgressBar()
            }

            override fun onFailure(call: Call<List<InventoryItem>>, t: Throwable) {
                handleNetworkError("Error loading inventory", t)
                activity.hideProgressBar()
            }
        })
    }

    /**
     * Update inventory lists for menu items and equipment
     * @param items List of inventory items to process
     */
    private fun updateInventoryLists(items: List<InventoryItem>) {
        try {
            val (menuItems, equipmentItems) = items.partition { item ->
                item.category.equals("Food", ignoreCase = true) ||
                        item.category.equals("Beverage", ignoreCase = true)
            }

            activity.runOnUiThread {
                activity.menuItemsAdapter.updateItems(menuItems.toMutableList())
                activity.equipmentAdapter.updateItems(equipmentItems.toMutableList())
                activity.updateAddEventButtonState()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating inventory lists", e)
            activity.showError(activity.getString(R.string.error_server))
        }
    }

    /**
     * Add a default admin user when no employees are found
     */
    private fun addDefaultAdmin() {
        activity.employees.clear()
        activity.employees.add(User(
            userId = ADMIN_USER_ID,
            username = "admin",
            firstName = "Admin",
            lastName = "User",
            role = "caterer"
        ))
    }

    /**
     * Create a new event
     * Validates inputs, prepares event data, and submits to the server
     */
    internal fun createEvent() {
        if (!activity.validateAllInputs()) return

        activity.showProgressBar()
        val client = activity.clientSpinner.selectedItem as? Client ?: run {
            activity.showError(activity.getString(R.string.error_client_required))
            activity.hideProgressBar()
            return
        }

        try {
            val dateStr = activity.eventDateInput.text.toString()
            val eventDateTime = activity.parseDateTime(dateStr, activity.eventStartTimeInput.text.toString())
                ?: throw IllegalStateException("Invalid date/time")

            val serverDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(eventDateTime.time)
            val startTime = "${activity.eventStartTimeInput.text}:00"
            val endTime = "${activity.eventEndTimeInput.text}:00"
            val name = activity.eventNameInput.text.toString().trim()
            val location = activity.eventLocationInput.text.toString().trim()
            val guests = activity.expectedGuestsInput.text.toString().toInt()

            DatabaseApi.retrofitService.addEvent(
                name = name,
                eventDate = serverDate,
                startTime = startTime,
                endTime = endTime,
                location = location,
                status = "pending",
                numberOfGuests = guests,
                clientId = client.id,
                employeeId = ADMIN_USER_ID,
                additionalInfo = createAdditionalInfo()
            ).enqueue(object : Callback<EventResponse> {
                override fun onResponse(
                    call: Call<EventResponse>,
                    response: Response<EventResponse>
                ) {
                    if (response.isSuccessful) {
                        val eventResponse = response.body()
                        if (eventResponse?.status == true && eventResponse.eventId != null) {
                            submitEventInventory(eventResponse.eventId)
                        } else {
                            Log.e(TAG, "Event creation failed: ${eventResponse?.message}")
                            activity.showError(eventResponse?.message ?: activity.getString(R.string.error_server))
                            activity.hideProgressBar()
                        }
                    } else {
                        handleErrorResponse("Failed to create event", response)
                        activity.hideProgressBar()
                    }
                }

                override fun onFailure(call: Call<EventResponse>, t: Throwable) {
                    handleNetworkError("Network error during event creation", t)
                    activity.hideProgressBar()
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error creating event", e)
            activity.showError(activity.getString(R.string.error_server))
            activity.hideProgressBar()
        }
    }

    /**
     * Submit event inventory items
     * @param eventId ID of the created event
     */
    internal fun submitEventInventory(eventId: Int) {
        val menuItems = activity.menuItemsAdapter.getSelectedItems()
        val equipmentItems = activity.equipmentAdapter.getSelectedItems()

        val inventoryJson = JSONArray().apply {
            (menuItems + equipmentItems).forEach { (item, quantity) ->
                put(JSONObject().apply {
                    put("inventory_id", item.id)
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
                    activity.showSuccess(activity.getString(R.string.success))
                    activity.clearInputs()
                } else {
                    handleErrorResponse("Failed to save inventory items", response)
                }
                activity.hideProgressBar()
            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                handleNetworkError("Network error while saving inventory", t)
                activity.hideProgressBar()
            }
        })
    }

    /**
     * Add a new client to the database
     * @param firstname Client's first name
     * @param lastname Client's last name
     * @param email Client's email address
     * @param phonenumber Client's phone number
     * @param onSuccess Callback for successful client addition
     * @param onError Callback for client addition failure
     */
    internal fun addClient(
        firstname: String,
        lastname: String,
        email: String,
        phonenumber: String,
        onSuccess: (AddClientResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        // Input validation
        if (firstname.isBlank() || lastname.isBlank() ||
            email.isBlank() || phonenumber.isBlank()) {
            onError("All fields must not be empty")
            return
        }

        // Sanitize inputs
        val sanitizedPhone = phonenumber.filter { it.isDigit() }
        if (sanitizedPhone.length < 7 || sanitizedPhone.length > 15) {
            onError("Invalid phone number format")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onError("Invalid email format")
            return
        }

        DatabaseApi.retrofitService.addClient(
            firstname = firstname.trim(),
            lastname = lastname.trim(),
            email = email.trim(),
            phonenumber = sanitizedPhone
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

    /**
     * Create additional information JSON for event creation
     * @return JSON string with event details
     */
    private fun createAdditionalInfo(): String {
        return JSONObject().apply {
            put("menu_items", activity.menuItemsAdapter.getSelectedItems().size)
            put("equipment_items", activity.equipmentAdapter.getSelectedItems().size)
            put("created_at", System.currentTimeMillis())
        }.toString()
    }

    /**
     * Handle error responses from network calls
     * @param message Error message
     * @param response Network response
     */
    private fun handleErrorResponse(message: String, response: Response<*>) {
        val errorBody = response.errorBody()?.string()
        Log.e(TAG, "$message: $errorBody")
        activity.showError(activity.getString(R.string.error_server))
    }

    /**
     * Handle network errors
     * @param message Error message
     * @param t Throwable containing error details
     */
    private fun handleNetworkError(message: String, t: Throwable) {
        Log.e(TAG, message, t)
        activity.showError(activity.getString(R.string.error_network))
    }
}