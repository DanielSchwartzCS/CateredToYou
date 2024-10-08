package com.example.cateredtoyou

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class TaskView : AppCompatActivity() {

    private lateinit var taskListView: ListView
    private lateinit var inputTask: EditText
    private lateinit var addTaskButton: Button
    private val taskList = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_taskview)

        // Find the back button and set the finish() style to return to the previous screen
        val backButton: Button = findViewById(R.id.back_to_MainActivity)
        backButton.setOnClickListener {
            finish() // Close this activity and return to the previous one (Dashboard)
        }

        // Initialize views
        taskListView = findViewById(R.id.todo_list_view)
        inputTask = findViewById(R.id.input_task)
        addTaskButton = findViewById(R.id.add_task_button)

        // Set up the adapter
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, taskList)
        taskListView.adapter = adapter

        // Add task button listener
        addTaskButton.setOnClickListener {
            val task = inputTask.text.toString()
            if (task.isNotEmpty()) {
                taskList.add(task)
                adapter.notifyDataSetChanged() // Refresh the list
                inputTask.text.clear() // Clear input field
            }
        }
    }
}
