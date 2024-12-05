
package com.example.cateredtoyou

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cateredtoyou.R
import com.example.cateredtoyou.apifiles.InventoryItem
import com.google.android.material.button.MaterialButton

class InventoryManagementAdapter(
    private val onEdit: (InventoryItem) -> Unit,
    private val onDelete: (InventoryItem) -> Unit
) : ListAdapter<InventoryItem, InventoryManagementAdapter.ViewHolder>(InventoryDiffCallback()) {

    private var originalList = listOf<InventoryItem>()
    private var currentSearchQuery = ""
    private var currentCategory: String? = null

    fun updateList(list: List<InventoryItem>) {
        originalList = list
        filter(currentSearchQuery)
    }

    fun filter(query: String) {
        currentSearchQuery = query
        applyFilters()
    }

    fun filterByCategory(category: String?) {
        currentCategory = category
        applyFilters()
    }

    private fun applyFilters() {
        var result = originalList

        if (!currentCategory.isNullOrEmpty()) {
            result = result.filter { it.category.equals(currentCategory, ignoreCase = true) }
        }

        if (currentSearchQuery.isNotEmpty()) {
            result = result.filter {
                it.item_name.contains(currentSearchQuery, ignoreCase = true) ||
                        it.category.contains(currentSearchQuery, ignoreCase = true) ||
                        it.notes?.contains(currentSearchQuery, ignoreCase = true) == true
            }
        }

        super.submitList(result)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventory_management, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.itemName)
        private val categoryText: TextView = itemView.findViewById(R.id.itemCategory)
        private val quantityText: TextView = itemView.findViewById(R.id.itemQuantity)
        private val costText: TextView = itemView.findViewById(R.id.itemCost)
        private val editButton: MaterialButton = itemView.findViewById(R.id.editButton)
        private val deleteButton: MaterialButton = itemView.findViewById(R.id.deleteButton)

        fun bind(item: InventoryItem) {
            nameText.text = item.item_name
            categoryText.text = item.category
            quantityText.text = "${item.quantity_in_stock} ${item.display_unit ?: "units"}"
            costText.text = String.format("$%.2f per unit", item.cost_per_unit)

            editButton.setOnClickListener { onEdit(item) }
            deleteButton.setOnClickListener { onDelete(item) }
        }
    }

    private class InventoryDiffCallback : DiffUtil.ItemCallback<InventoryItem>() {
        override fun areItemsTheSame(oldItem: InventoryItem, newItem: InventoryItem): Boolean {
            return oldItem.inventory_id == newItem.inventory_id
        }

        override fun areContentsTheSame(oldItem: InventoryItem, newItem: InventoryItem): Boolean {
            return oldItem == newItem
        }
    }
}