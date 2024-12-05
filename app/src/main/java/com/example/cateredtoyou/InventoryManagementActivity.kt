package com.example.cateredtoyou

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.cateredtoyou.apifiles.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.json.JSONObject

class InventoryManagementActivity : AppCompatActivity() {
    private val validCategories = listOf("Food", "Beverage", "Equipment", "Raw", "Utensil")
    private val validDisplayUnits = listOf("oz", "lb", "kg", "ea")
    private val defaultStorageLocationId = 1

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: InventoryManagementAdapter // Using your existing adapter
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var noDataText: TextView
    private lateinit var addFab: FloatingActionButton
    private lateinit var searchView: SearchView
    private lateinit var categorySpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_management)
        setupViews()
        setupListeners()
        fetchInventoryData()
    }

    private fun setupViews() {
        // Initialize views
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        noDataText = findViewById(R.id.noDataText)
        addFab = findViewById(R.id.addFab)
        searchView = findViewById(R.id.searchView)
        categorySpinner = findViewById(R.id.categorySpinner)

        // Setup adapter
        adapter = InventoryManagementAdapter(
            onEdit = { showEditDialog(it) },
            onDelete = { showDeleteConfirmation(it) }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@InventoryManagementActivity)
            adapter = this@InventoryManagementActivity.adapter
        }

        // Setup category spinner
        val categories = listOf("All") + validCategories
        ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        ).also { spinnerAdapter ->
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = spinnerAdapter
        }
    }

    private fun setupListeners() {
        swipeRefresh.setOnRefreshListener { fetchInventoryData() }

        addFab.setOnClickListener { showAddDialog() }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return true
            }
        })

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val category = if (position == 0) null else validCategories[position - 1]
                adapter.filterByCategory(category)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                adapter.filterByCategory(null)
            }
        }
    }

    private fun showAddDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_inventory_item, null)
        setupInventoryDialog(dialogView, null) { newItem ->
            showLoading(true)
            DatabaseApi.retrofitService.addInventoryItem(newItem).enqueue(object : Callback<BaseResponse> {
                override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
                    handleInventoryResponse(response, "Item added successfully", "Failed to add item")
                }

                override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                    handleInventoryFailure(t)
                }
            })
        }
    }

    private fun showEditDialog(item: InventoryItem) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_inventory_item, null)
        setupInventoryDialog(dialogView, item) { updatedItem ->
            showLoading(true)
            DatabaseApi.retrofitService.updateInventoryItem(item.inventory_id, updatedItem)
                .enqueue(object : Callback<BaseResponse> {
                    override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
                        handleInventoryResponse(response, "Item updated successfully", "Failed to update item")
                    }

                    override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                        handleInventoryFailure(t)
                    }
                })
        }
    }

    private fun setupInventoryDialog(
        dialogView: View,
        existingItem: InventoryItem?,
        onSubmit: (InventoryItem) -> Unit
    ) {
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.itemNameInput)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val quantityInput = dialogView.findViewById<TextInputEditText>(R.id.quantityInput)
        val costInput = dialogView.findViewById<TextInputEditText>(R.id.costInput)
        val displayUnitSpinner = dialogView.findViewById<Spinner>(R.id.displayUnitSpinner)
        val notesInput = dialogView.findViewById<TextInputEditText>(R.id.notesInput)

        // Setup spinners
        ArrayAdapter(this, android.R.layout.simple_spinner_item, validCategories).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = it
        }

        ArrayAdapter(this, android.R.layout.simple_spinner_item, validDisplayUnits).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            displayUnitSpinner.adapter = it
        }

        // Set existing values if editing
        existingItem?.let { item ->
            nameInput.setText(item.item_name)
            quantityInput.setText(item.quantity_in_stock.toString())
            costInput.setText(item.cost_per_unit.toString())
            notesInput.setText(item.notes)

            validCategories.indexOf(item.category).takeIf { it >= 0 }?.let {
                categorySpinner.setSelection(it)
            }
            item.display_unit?.let { unit ->
                validDisplayUnits.indexOf(unit).takeIf { it >= 0 }?.let {
                    displayUnitSpinner.setSelection(it)
                }
            }
        }

        AlertDialog.Builder(this)
            .setTitle(if (existingItem == null) "Add Item" else "Edit Item")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                if (validateInputs(nameInput, quantityInput, costInput, categorySpinner)) {
                    val newItem = InventoryItem(
                        inventory_id = existingItem?.inventory_id ?: 0,
                        item_name = nameInput.text.toString().trim(),
                        category = categorySpinner.selectedItem.toString(),
                        quantity_in_stock = quantityInput.text.toString().toFloatOrNull() ?: 0f,
                        cost_per_unit = costInput.text.toString().toFloatOrNull() ?: 0f,
                        display_unit = displayUnitSpinner.selectedItem.toString(),
                        location_id = defaultStorageLocationId,
                        notes = notesInput.text.toString().trim()
                    )
                    onSubmit(newItem)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun validateInputs(
        nameInput: TextInputEditText,
        quantityInput: TextInputEditText,
        costInput: TextInputEditText,
        categorySpinner: Spinner
    ): Boolean {
        when {
            nameInput.text.isNullOrBlank() -> {
                showToast("Please enter item name")
                return false
            }
            quantityInput.text.isNullOrBlank() || quantityInput.text.toString().toFloatOrNull() == null -> {
                showToast("Please enter valid quantity")
                return false
            }
            costInput.text.isNullOrBlank() || costInput.text.toString().toFloatOrNull() == null -> {
                showToast("Please enter valid cost")
                return false
            }
            categorySpinner.selectedItemPosition < 0 -> {
                showToast("Please select a category")
                return false
            }
        }
        return true
    }

    private fun showDeleteConfirmation(item: InventoryItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete ${item.item_name}?")
            .setPositiveButton("Delete") { _, _ -> deleteInventoryItem(item) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteInventoryItem(item: InventoryItem) {
        showLoading(true)
        DatabaseApi.retrofitService.deleteInventoryItem(item.inventory_id)
            .enqueue(object : Callback<BaseResponse> {
                override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
                    handleInventoryResponse(response, "Item deleted successfully", "Failed to delete item")
                }

                override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                    handleInventoryFailure(t)
                }
            })
    }

    private fun handleInventoryResponse(response: Response<BaseResponse>, successMsg: String, failureMsg: String) {
        showLoading(false)
        if (response.isSuccessful && response.body()?.status == true) {
            showToast(successMsg)
            fetchInventoryData()
        } else {
            try {
                val errorBody = response.errorBody()?.string()
                val errorJson = JSONObject(errorBody ?: "")
                showToast(errorJson.optString("message", failureMsg))
            } catch (e: Exception) {
                showToast(failureMsg)
            }
        }
    }

    private fun handleInventoryFailure(t: Throwable) {
        showLoading(false)
        Log.e("InventoryManagement", "API call failed", t)
        showToast("Network error occurred")
    }

    private fun fetchInventoryData() {
        showLoading(true)
        DatabaseApi.retrofitService.getInventory().enqueue(object : Callback<List<InventoryItem>> {
            override fun onResponse(call: Call<List<InventoryItem>>, response: Response<List<InventoryItem>>) {
                showLoading(false)
                if (response.isSuccessful) {
                    updateInventoryDisplay(response.body() ?: emptyList())
                } else {
                    showToast("Failed to load inventory")
                }
            }

            override fun onFailure(call: Call<List<InventoryItem>>, t: Throwable) {
                showLoading(false)
                showToast("Network error occurred")
            }
        })
    }

    private fun updateInventoryDisplay(items: List<InventoryItem>) {
        noDataText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        adapter.updateList(items)
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        swipeRefresh.isRefreshing = false
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}