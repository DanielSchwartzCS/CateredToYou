package com.example.cateredtoyou

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.NumberPicker
import android.widget.TextView
import com.example.cateredtoyou.apifiles.InventoryItem

class InventoryAdapter(
    private val context: Context,
    private var items: MutableList<InventoryItem>,
    private val onQuantityChanged: (InventoryItem, Int) -> Unit
) : BaseAdapter() {
    private val selectedQuantities = mutableMapOf<Int, Int>()

    fun updateItems(newItems: List<InventoryItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun clearSelections() {
        selectedQuantities.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItems(): Map<InventoryItem, Int> {
        return items.filter { selectedQuantities[it.id] ?: 0 > 0 }
            .associateWith { selectedQuantities[it.id] ?: 0 }
    }

    override fun getCount(): Int = items.size
    override fun getItem(position: Int): InventoryItem = items[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_inventory, parent, false)

        val item = getItem(position)

        // Main item name and category
        view.findViewById<TextView>(R.id.itemName).apply {
            text = "${item.itemName} (${item.category})"
        }

        // Details including quantity, unit, and cost
        view.findViewById<TextView>(R.id.itemDetails).apply {
            val cost = item.costPerUnit?.let { "@ $${String.format("%.2f", it)}/unit" } ?: ""
            text = "${item.quantity} ${item.unitOfMeasurement ?: "units"} available $cost"
        }

        // Notes if available
        view.findViewById<TextView>(R.id.itemNotes).apply {
            if (!item.notes.isNullOrBlank()) {
                text = item.notes
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }

        // Quantity picker
        view.findViewById<NumberPicker>(R.id.quantityPicker).apply {
            minValue = 0
            maxValue = item.quantity
            value = selectedQuantities[item.id] ?: 0
            setOnValueChangedListener { _, _, newVal ->
                selectedQuantities[item.id] = newVal
                onQuantityChanged(item, newVal)
            }
        }

        return view
    }
}