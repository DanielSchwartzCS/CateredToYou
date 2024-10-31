package com.example.cateredtoyou

import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecipeActivity : AppCompatActivity() {

    private lateinit var recipeSpinner: Spinner
    private lateinit var recipeTextView: TextView
    private lateinit var backButton: Button

    // Example recipes with ingredients and instructions
    private val recipes = listOf(
        "Spaghetti Bolognese" to {
            recipeTextView.text = "Ingredients:\n" +
                    "- Spaghetti\n" +
                    "- Ground Beef\n" +
                    "- Tomato Sauce\n" +
                    "- Onion\n" +
                    "- Garlic\n\n" +
                    "Instructions:\n" +
                    "1. Cook spaghetti according to package instructions.\n" +
                    "2. In a pan, sauté onion and garlic, then add ground beef.\n" +
                    "3. Stir in tomato sauce and simmer.\n" +
                    "4. Serve sauce over spaghetti."
        },
        "Chicken Alfredo" to {
            recipeTextView.text = "Ingredients:\n" +
                    "- Fettuccine\n" +
                    "- Chicken Breast\n" +
                    "- Cream\n" +
                    "- Parmesan Cheese\n" +
                    "- Garlic\n\n" +
                    "Instructions:\n" +
                    "1. Cook fettuccine according to package instructions.\n" +
                    "2. Sauté chicken until cooked, add garlic, then cream.\n" +
                    "3. Stir in Parmesan cheese until melted.\n" +
                    "4. Serve sauce over fettuccine."
        },
        "Tacos" to {
            recipeTextView.text = "Ingredients:\n" +
                    "- Taco Shells\n" +
                    "- Ground Beef\n" +
                    "- Lettuce\n" +
                    "- Tomato\n" +
                    "- Cheese\n\n" +
                    "Instructions:\n" +
                    "1. Cook ground beef with taco seasoning.\n" +
                    "2. Fill taco shells with beef and toppings."
        },
        "Caesar Salad" to {
            recipeTextView.text = "Ingredients:\n" +
                    "- Romaine Lettuce\n" +
                    "- Croutons\n" +
                    "- Caesar Dressing\n" +
                    "- Parmesan Cheese\n\n" +
                    "Instructions:\n" +
                    "1. Toss romaine lettuce with dressing.\n" +
                    "2. Top with croutons and Parmesan."
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe)

        recipeSpinner = findViewById(R.id.spinner_recipes)
        recipeTextView = findViewById(R.id.recipe_text_view)
        backButton = findViewById(R.id.btn_back)

        // Set up the spinner adapter
        val recipeNames = recipes.map { it.first } // Get only the recipe names for the spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, recipeNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        recipeSpinner.adapter = adapter

        // Set the default selection to the first recipe
        recipeSpinner.setSelection(0)
        recipes[0].second.invoke() // Display the first recipe by default

        // Set the spinner item selected listener
        recipeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View, position: Int, id: Long) {
                recipes[position].second.invoke() // Invoke the recipe display for the selected recipe
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optional: Handle case where nothing is selected if needed
            }
        }

        backButton.setOnClickListener {
            finish() // Navigate back to the previous activity
        }
    }
}
