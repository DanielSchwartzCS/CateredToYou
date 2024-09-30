package com.example.cateredtoyou

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class InventoryActivity : AppCompatActivity() {

    private lateinit var itemQuantityTextViewBeef: TextView
    private lateinit var itemQuantityTextViewChicken: TextView
    private lateinit var itemQuantityTextViewPork: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory)

        // Initialize views
        itemQuantityTextViewBeef = findViewById(R.id.item_quantity_beef)
        itemQuantityTextViewChicken = findViewById(R.id.item_quantity_chicken)
        itemQuantityTextViewPork = findViewById(R.id.item_quantity_pork)

        // Set up click listeners for Beef
        findViewById<Button>(R.id.increase_button_beef).setOnClickListener {
            updateQuantity(itemQuantityTextViewBeef, 1)
        }
        findViewById<Button>(R.id.decrease_button_beef).setOnClickListener {
            updateQuantity(itemQuantityTextViewBeef, -1)
        }

        // Set up click listeners for Chicken
        findViewById<Button>(R.id.increase_button_chicken).setOnClickListener {
            updateQuantity(itemQuantityTextViewChicken, 1)
        }
        findViewById<Button>(R.id.decrease_button_chicken).setOnClickListener {
            updateQuantity(itemQuantityTextViewChicken, -1)
        }

        // Set up click listeners for Pork
        findViewById<Button>(R.id.increase_button_pork).setOnClickListener {
            updateQuantity(itemQuantityTextViewPork, 1)
        }
        findViewById<Button>(R.id.decrease_button_pork).setOnClickListener {
            updateQuantity(itemQuantityTextViewPork, -1)
        }
    }

    private fun updateQuantity(textView: TextView, change: Int) {
        val currentText = textView.text.toString()
        val currentQuantity = currentText.substringAfter(": ").toInt()
        val newQuantity = maxOf(0, currentQuantity + change)
        textView.text = "Quantity: $newQuantity"
    }
}