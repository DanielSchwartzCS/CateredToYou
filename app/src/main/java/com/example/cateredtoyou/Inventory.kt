package com.example.cateredtoyou

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Inventory : AppCompatActivity() {



    private var quantities = mutableMapOf("Beef" to 0, "Chicken" to 0, "Pork" to 0)
    private lateinit var itemQuantityTextViewBeef: TextView
    private lateinit var itemQuantityTextViewChicken: TextView
    private lateinit var itemQuantityTextViewPork: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory)

        // Find the back button and set the finish() style to return to the previous screen
        val backButton: Button = findViewById(R.id.back_to_dashboard)
        backButton.setOnClickListener {
            finish() // Close this activity and return to the previous one (Dashboard)
        }

        val itemNameTextViewBeef: TextView = findViewById(R.id.item_name_beef)
        itemQuantityTextViewBeef = findViewById(R.id.item_quantity_beef)
        val increaseButtonBeef: Button = findViewById(R.id.increase_button_beef)
        val decreaseButtonBeef: Button = findViewById(R.id.decrease_button_beef)

        val itemNameTextViewChicken: TextView = findViewById(R.id.item_name_chicken)
        itemQuantityTextViewChicken = findViewById(R.id.item_quantity_chicken)
        val increaseButtonChicken: Button = findViewById(R.id.increase_button_chicken)
        val decreaseButtonChicken: Button = findViewById(R.id.decrease_button_chicken)

        val itemNameTextViewPork: TextView = findViewById(R.id.item_name_pork)
        itemQuantityTextViewPork = findViewById(R.id.item_quantity_pork)
        val increaseButtonPork: Button = findViewById(R.id.increase_button_pork)
        val decreaseButtonPork: Button = findViewById(R.id.decrease_button_pork)

        // Set initial quantities
        updateQuantity("Beef")
        updateQuantity("Chicken")
        updateQuantity("Pork")

        increaseButtonBeef.setOnClickListener {
            quantities["Beef"] = quantities["Beef"]!! + 1
            updateQuantity("Beef")
        }

        decreaseButtonBeef.setOnClickListener {
            if (quantities["Beef"]!! > 0) {
                quantities["Beef"] = quantities["Beef"]!! - 1
                updateQuantity("Beef")
            }
        }

        increaseButtonChicken.setOnClickListener {
            quantities["Chicken"] = quantities["Chicken"]!! + 1
            updateQuantity("Chicken")
        }

        decreaseButtonChicken.setOnClickListener {
            if (quantities["Chicken"]!! > 0) {
                quantities["Chicken"] = quantities["Chicken"]!! - 1
                updateQuantity("Chicken")
            }
        }

        increaseButtonPork.setOnClickListener {
            quantities["Pork"] = quantities["Pork"]!! + 1
            updateQuantity("Pork")
        }

        decreaseButtonPork.setOnClickListener {
            if (quantities["Pork"]!! > 0) {
                quantities["Pork"] = quantities["Pork"]!! - 1
                updateQuantity("Pork")
            }
        }
    }

    private fun updateQuantity(item: String) {
        when (item) {
            "Beef" -> itemQuantityTextViewBeef.text = "Quantity: ${quantities[item]}"
            "Chicken" -> itemQuantityTextViewChicken.text = "Quantity: ${quantities[item]}"
            "Pork" -> itemQuantityTextViewPork.text = "Quantity: ${quantities[item]}"
        }
    }
}
