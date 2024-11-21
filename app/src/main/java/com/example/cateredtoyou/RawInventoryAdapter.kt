// RawInventoryAdapter.kt
package com.example.cateredtoyou.adapters

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

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Handler
import android.os.Looper

class RawInventoryAdapter(
    private val onQuantityChanged: (InventoryItem, Int) -> Unit
) : ListAdapter<InventoryItem, RawInventoryAdapter.ViewHolder>(InventoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_raw_inventory, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemName: TextView = itemView.findViewById(R.id.itemName)
        private val itemQuantity: TextView = itemView.findViewById(R.id.itemQuantity)
        private val minimumStock: TextView = itemView.findViewById(R.id.minimumStock)
        private val increaseButton: MaterialButton = itemView.findViewById(R.id.increaseButton)
        private val decreaseButton: MaterialButton = itemView.findViewById(R.id.decreaseButton)
        private val lowStockWarning: TextView = itemView.findViewById(R.id.lowStockWarning)

        fun bind(item: InventoryItem) {
            itemName.text = item.itemName
            updateQuantityText(item)
            updateMinimumStockText(item)
            updateLowStockWarning(item)

            increaseButton.setOnClickListener {
                applyButtonAnimations(increaseButton)
                onQuantityChanged(item, item.quantity + 1)
            }

            decreaseButton.setOnClickListener {
                applyButtonAnimations(decreaseButton)
                if (item.quantity > 0) {
                    onQuantityChanged(item, item.quantity - 1)
                }
            }
        }

        private fun applyButtonAnimations(button: MaterialButton) {
            button.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(100)
                .withEndAction {
                    button.animate().scaleX(1.0f).scaleY(1.0f).duration = 100
                }

            button.setBackgroundColor(Color.LTGRAY)
            Handler(Looper.getMainLooper()).postDelayed({
                button.setBackgroundColor(Color.WHITE)
            }, 100)
        }

        private fun updateQuantityText(item: InventoryItem) {
            val unit = item.unitOfMeasurement ?: "units"
            itemQuantity.text = "Quantity: ${item.quantity} $unit"
        }

        private fun updateMinimumStockText(item: InventoryItem) {
            item.minimumStock?.let { minStock ->
                val unit = item.unitOfMeasurement ?: "units"
                minimumStock.text = "Minimum Stock: $minStock $unit"
                minimumStock.visibility = View.VISIBLE
            } ?: run {
                minimumStock.visibility = View.GONE
            }
        }

        private fun updateLowStockWarning(item: InventoryItem) {
            item.minimumStock?.let { minStock ->
                lowStockWarning.visibility = if (item.quantity <= minStock) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            } ?: run {
                lowStockWarning.visibility = View.GONE
            }
        }
    }

    private class InventoryDiffCallback : DiffUtil.ItemCallback<InventoryItem>() {
        override fun areItemsTheSame(oldItem: InventoryItem, newItem: InventoryItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: InventoryItem, newItem: InventoryItem): Boolean {
            return oldItem == newItem
        }
    }
}
