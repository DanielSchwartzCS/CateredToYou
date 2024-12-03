package com.example.cateredtoyou

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.NumberPicker
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.cateredtoyou.apifiles.InventoryItem
import com.google.android.material.chip.Chip
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class InventoryAdapter(
    private val context: Context,
    private var items: List<InventoryItem>,
    private val onQuantityChanged: (InventoryItem, Int, Double) -> Unit
) : BaseAdapter() {

    private val selectedQuantities = mutableMapOf<Int, Int>()
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US).apply {
        currency = Currency.getInstance("USD")
    }
    private var currentCategory: String? = null
    private var filteredItems = items
    private var totalCost: Double = 0.0
    private val updateHandler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    fun updateItems(newItems: List<InventoryItem>) {
        items = newItems
        applyFilter()
        notifyDataSetChanged()
    }

    fun filterByCategory(category: String?) {
        currentCategory = category
        applyFilter()
        notifyDataSetChanged()
    }

    private fun applyFilter() {
        filteredItems = if (currentCategory == null) {
            items
        } else {
            items.filter { it.category.equals(currentCategory, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    fun clearSelections() {
        selectedQuantities.clear()
        totalCost = 0.0
        notifyDataSetChanged()
    }

    fun getSelectedItems(): Map<InventoryItem, Int> = buildMap {
        selectedQuantities.filterValues { it > 0 }.forEach { (id, quantity) ->
            items.find { it.inventory_id == id }?.let { item ->
                put(item, quantity)
            }
        }
    }

    fun getTotalCost(): Double = totalCost

    fun getTotalItems(): Int = selectedQuantities.values.sum()

    override fun getCount(): Int = filteredItems.size
    override fun getItem(position: Int): InventoryItem = filteredItems[position]
    override fun getItemId(position: Int): Long = filteredItems[position].inventory_id.toLong()
    override fun hasStableIds(): Boolean = true

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_inventory, parent, false)
        val viewHolder = view.tag as? ViewHolder ?: ViewHolder(view)
        val item = getItem(position)
        viewHolder.bind(item)
        return view
    }

    private inner class ViewHolder(itemView: View) {
        val cardView: CardView = itemView.findViewById(R.id.card_view)
        val nameTextView: TextView = itemView.findViewById(R.id.item_name)
        val detailsTextView: TextView = itemView.findViewById(R.id.item_details)
        val notesTextView: TextView = itemView.findViewById(R.id.item_notes)
        val quantityPicker: NumberPicker = itemView.findViewById(R.id.quantity_picker)
        val categoryChip: Chip = itemView.findViewById(R.id.category_chip)
        val availabilityIndicator: View = itemView.findViewById(R.id.availability_indicator)

        init {
            itemView.tag = this
        }

        fun bind(item: InventoryItem) {
            Log.d("InventoryAdapter", "Binding item: ${item.item_name} with selected quantity: ${selectedQuantities[item.inventory_id]}")
            setupBasicInfo(item)
            setupQuantityPicker(item)
            setupCategoryChip(item)
            setupAvailabilityIndicator(item)
            setupCardState(item)

        }

        private fun setupBasicInfo(item: InventoryItem) {
            nameTextView.text = item.item_name

//            val cost = ""
            val cost = item.cost_per_unit?.let {
                currencyFormatter.format(it)
            } ?: ""

            detailsTextView.text = buildString {
                append(item.quantity_in_stock)
                append(" ")
                append(item.display_unit ?: "units")
                append(" available ")
                if (cost.isNotEmpty()) {
                    append("@ ")
                    append(cost)
                }
            }

            notesTextView.apply {
                text = item.notes
                visibility = if (item.notes.isNullOrBlank()) View.GONE else View.VISIBLE
            }
        }

        private fun setupQuantityPicker(item: InventoryItem) {
            quantityPicker.apply {
                minValue = 0
                maxValue = item.quantity_in_stock.toInt()

                setOnValueChangedListener(null)

                value = selectedQuantities[item.inventory_id] ?: 0


                setOnValueChangedListener { _, _, newVal ->
                    updateQuantity(item, newVal)
                }
            }
        }

        private fun updateQuantity(item: InventoryItem, newVal: Int) {
            selectedQuantities[item.inventory_id] = newVal

            Log.d("InventoryAdapter", "Updated ${item.item_name} to $newVal")
            Log.d("InventoryAdapter", "All selections: $selectedQuantities")

            // Debounce the updates to prevent rapid firing
            updateRunnable?.let { updateHandler.removeCallbacks(it) }
            updateRunnable = Runnable {
                calculateTotalCost()
                onQuantityChanged(item, newVal, totalCost)
            }.also {
                updateHandler.postDelayed(it, 300)
            }
        }

        private fun setupCategoryChip(item: InventoryItem) {
            categoryChip.apply {
                text = item.category
                setChipBackgroundColorResource(getCategoryColorRes(item.category))
                setTextColor(Color.WHITE)
            }
        }

        private fun setupAvailabilityIndicator(item: InventoryItem) {
            val colorRes = when {
                item.quantity_in_stock.toInt() == 0 -> android.R.color.holo_red_dark
                item.quantity_in_stock < 5 -> android.R.color.holo_orange_dark
                else -> android.R.color.holo_green_dark
            }
            availabilityIndicator.setBackgroundColor(ContextCompat.getColor(context, colorRes))
        }

        private fun setupCardState(item: InventoryItem) {
            cardView.apply {
                setCardBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        if ((selectedQuantities[item.inventory_id] ?: 0) > 0) {
                            R.color.selected_card_background
                        } else {
                            android.R.color.white
                        }
                    )
                )
            }
        }
    }

    private fun getCategoryColorRes(category: String): Int {
        return when (category.lowercase()) {
            "food" -> android.R.color.holo_blue_dark
            "beverage" -> android.R.color.holo_green_dark
            "equipment" -> android.R.color.holo_orange_dark
            "utensil" -> android.R.color.holo_purple
            "decoration" -> android.R.color.holo_red_light
            else -> android.R.color.darker_gray
        }
    }

    private fun calculateTotalCost() {
        totalCost = selectedQuantities.entries.sumOf { (itemId, quantity) ->
            items.find { it.inventory_id == itemId }?.let { item ->
                (item.cost_per_unit.toDouble() ?: 0.0) * quantity
            } ?: 0.0
        }
    }
}