package com.example.cateredtoyou

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cateredtoyou.apifiles.InventoryItem
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import android.widget.NumberPicker
import android.widget.RadioGroup
import androidx.core.content.ContextCompat

class InventoryManagementAdapter(
    private val onEdit: (InventoryItem) -> Unit,
    private val onUpdateQuantity: (InventoryItem, Float) -> Unit
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

        submitList(result)
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
        private val card: MaterialCardView = itemView.findViewById(R.id.itemCard)
        private val nameText: TextView = itemView.findViewById(R.id.itemName)
        private val categoryText: TextView = itemView.findViewById(R.id.itemCategory)
        private val quantityText: TextView = itemView.findViewById(R.id.itemQuantity)
        private val costText: TextView = itemView.findViewById(R.id.itemCost)
        private val notesText: TextView = itemView.findViewById(R.id.itemNotes)
        private val updateButton: MaterialButton = itemView.findViewById(R.id.updateButton)
        private val editButton: MaterialButton = itemView.findViewById(R.id.editButton)

        fun bind(item: InventoryItem) {
            // Apply card styling based on stock levels
            val context = itemView.context
            when {
                item.quantity_in_stock <= 10 -> {
                    card.strokeColor = ContextCompat.getColor(context, R.color.low_stock_warning)
                    card.strokeWidth = context.resources.getDimensionPixelSize(R.dimen.card_stroke_width)
                }
                item.quantity_in_stock <= 20 -> {
                    card.strokeColor = ContextCompat.getColor(context, R.color.medium_stock_warning)
                    card.strokeWidth = context.resources.getDimensionPixelSize(R.dimen.card_stroke_width)
                }
                else -> {
                    card.strokeColor = ContextCompat.getColor(context, R.color.normal_stock)
                    card.strokeWidth = context.resources.getDimensionPixelSize(R.dimen.card_stroke_width_normal)
                }
            }

            nameText.text = item.item_name
            categoryText.text = "Category: ${item.category}"
            quantityText.text = buildString {
                append("Quantity: ${item.quantity_in_stock}")
                if (!item.display_unit.isNullOrBlank()) {
                    append(" ${item.display_unit}")
                }
            }
            costText.text = String.format("Cost: $%.2f per unit", item.cost_per_unit)

            if (!item.notes.isNullOrBlank()) {
                notesText.visibility = View.VISIBLE
                notesText.text = "Notes: ${item.notes}"
            } else {
                notesText.visibility = View.GONE
            }

            updateButton.setOnClickListener { showQuantityUpdateDialog(context, item) }
            editButton.setOnClickListener { onEdit(item) }
            card.setOnClickListener { showItemDetails(context, item) }
        }

        private fun showQuantityUpdateDialog(context: Context, item: InventoryItem) {
            val dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_quantity_update, null)

            val currentQuantityText = dialogView.findViewById<TextView>(R.id.currentQuantityText)
            val resultText = dialogView.findViewById<TextView>(R.id.resultText)
            val numberPicker = dialogView.findViewById<NumberPicker>(R.id.quantityPicker).apply {
                minValue = 1
                maxValue = 200
                value = 1
                wrapSelectorWheel = false
            }
            val operationGroup = dialogView.findViewById<RadioGroup>(R.id.operationGroup)

            fun updatePreviewText() {
                val change = numberPicker.value.toFloat()
                val newQuantity = when (operationGroup.checkedRadioButtonId) {
                    R.id.radioAdd -> item.quantity_in_stock + change
                    R.id.radioSubtract -> maxOf(0f, item.quantity_in_stock - change)
                    else -> change
                }

                currentQuantityText.text = "Current: ${item.quantity_in_stock} ${item.display_unit ?: ""}"
                resultText.text = "New: $newQuantity ${item.display_unit ?: ""}"
            }

            numberPicker.setOnValueChangedListener { _, _, _ -> updatePreviewText() }
            operationGroup.setOnCheckedChangeListener { _, _ -> updatePreviewText() }
            updatePreviewText()

            AlertDialog.Builder(context)
                .setTitle("Update ${item.item_name}")
                .setView(dialogView)
                .setPositiveButton("Update") { _, _ ->
                    val change = numberPicker.value.toFloat()
                    val newQuantity = when (operationGroup.checkedRadioButtonId) {
                        R.id.radioAdd -> item.quantity_in_stock + change
                        R.id.radioSubtract -> maxOf(0f, item.quantity_in_stock - change)
                        else -> change
                    }
                    // Keep the existing storage_location_id when updating
                    val updatedItem = item.copy(
                        quantity_in_stock = newQuantity,
                        location_id = item.location_id // Preserve the original location ID
                    )
                    onUpdateQuantity(updatedItem, newQuantity)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        private fun showItemDetails(context: Context, item: InventoryItem) {
            AlertDialog.Builder(context)
                .setTitle(item.item_name)
                .setMessage("""
                    Category: ${item.category}
                    Quantity: ${item.quantity_in_stock} ${item.display_unit ?: ""}
                    Cost: $${item.cost_per_unit} per unit
                    Location ID: ${item.location_id}
                    ${if (!item.notes.isNullOrBlank()) "\nNotes: ${item.notes}" else ""}
                """.trimIndent())
                .setPositiveButton("Close", null)
                .show()
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
