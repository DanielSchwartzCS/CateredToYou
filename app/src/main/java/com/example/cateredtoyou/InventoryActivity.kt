package com.example.cateredtoyou

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.cateredtoyou.adapters.RawInventoryAdapter
import com.example.cateredtoyou.apifiles.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InventoryActivity : AppCompatActivity() {
    private val TAG = "InventoryActivity"

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RawInventoryAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var noDataText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory)

        setupViews()
        fetchInventoryData()
    }

    private fun setupViews() {
        // Initialize views
        progressBar = findViewById(R.id.progressBar)
        recyclerView = findViewById(R.id.recyclerView)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        noDataText = findViewById(R.id.noDataText)

        // Setup back button
        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Setup RecyclerView
        adapter = RawInventoryAdapter(::onQuantityChanged)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@InventoryActivity)
            adapter = this@InventoryActivity.adapter
        }

        // Setup SwipeRefreshLayout
        swipeRefresh.setOnRefreshListener {
            fetchInventoryData()
        }
    }

    private fun fetchInventoryData() {
        showLoading(true)

        DatabaseApi.retrofitService.getRawInventory().enqueue(object : Callback<List<InventoryItem>> {
            override fun onResponse(call: Call<List<InventoryItem>>, response: Response<List<InventoryItem>>) {
                showLoading(false)
                if (response.isSuccessful) {
                    val items = response.body() ?: emptyList()
                    // Filter only Raw category items
                    val rawItems = items.filter { it.category == "Raw" }
                    updateInventoryDisplay(rawItems)
                } else {
                    showError("Failed to load inventory: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<InventoryItem>>, t: Throwable) {
                showLoading(false)
                showError("Network error: ${t.message}")
                Log.e(TAG, "Failed to fetch inventory", t)
            }
        })
    }

    private fun updateInventoryDisplay(items: List<InventoryItem>) {
        if (items.isEmpty()) {
            showNoData(true)
        } else {
            showNoData(false)
            adapter.submitList(items)
        }
    }

    private fun onQuantityChanged(item: InventoryItem, newQuantity: Int) {
        if (newQuantity < 0) {
            showError("Quantity cannot be negative")
            return
        }

        showLoading(true)

        val updateRequest = UpdateInventoryRequest(item.id, newQuantity)
        DatabaseApi.retrofitService.updateInventory(updateRequest)
            .enqueue(object : Callback<BaseResponse> {
                override fun onResponse(
                    call: Call<BaseResponse>,
                    response: Response<BaseResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        // Update successful, refresh the data
                        fetchInventoryData()
                    } else {
                        showLoading(false)
                        showError("Failed to update quantity")
                        // Refresh to ensure UI is in sync
                        fetchInventoryData()
                    }
                }

                override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                    showLoading(false)
                    showError("Network error while updating quantity")
                    Log.e(TAG, "Failed to update inventory", t)
                    // Refresh to ensure UI is in sync
                    fetchInventoryData()
                }
            })
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        if (!show) {
            swipeRefresh.isRefreshing = false
        }
    }

    private fun showNoData(show: Boolean) {
        noDataText.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}