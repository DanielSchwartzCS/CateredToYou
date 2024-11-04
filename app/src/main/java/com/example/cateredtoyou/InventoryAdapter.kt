package com.example.cateredtoyou

import android.content.Context
import android.util.Log
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
        Log.d("InventoryAdapter", "Updated with ${items.size} items: ${items.map { it.itemName }}")
        notifyDataSetChanged()
    }

    fun clearSelections() {
        selectedQuantities.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItems(): Map<InventoryItem, Int> {
        return items.filter { (selectedQuantities[it.id] ?: 0) > 0 }
            .associateWith { selectedQuantities[it.id] ?: 0 }
    }

    override fun getCount(): Int = items.size
    override fun getItem(position: Int): InventoryItem = items[position]
    override fun getItemId(position: Int): Long = items[position].id.toLong()
    override fun hasStableIds(): Boolean = true

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_inventory, parent, false)

        val item = getItem(position)

        // Log item being displayed
        Log.d("InventoryAdapter", "Displaying item at position $position: ${item.itemName}")

        view.findViewById<TextView>(R.id.itemName).apply {
            text = "${item.itemName} (${item.category})"
        }

        view.findViewById<TextView>(R.id.itemDetails).apply {
            val cost = item.costPerUnit?.let { "@ $${String.format("%.2f", it)}" } ?: ""
            text = "${item.quantity} ${item.unitOfMeasurement ?: "units"} available $cost"
        }

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