package com.example.cateredtoyou

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity

class TaskView : AppCompatActivity() {

    private lateinit var taskListView: ListView
    private lateinit var inputTask: EditText
    private lateinit var addTaskButton: Button
    private lateinit var searchView: SearchView // Add a SearchView for task filtering
    private val taskList = ArrayList<TaskItem>() // Use TaskItem
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_taskview)

        // Find views
        taskListView = findViewById(R.id.todo_list_view)
        inputTask = findViewById(R.id.input_task)
        addTaskButton = findViewById(R.id.add_task_button)
        searchView = findViewById(R.id.search_view) // Find the SearchView

        // Set up the TaskAdapter
        adapter = TaskAdapter(this, taskList)
        taskListView.adapter = adapter

        // Add task button listener
        addTaskButton.setOnClickListener {
            val taskName = inputTask.text.toString()
            if (taskName.isNotEmpty()) {
                taskList.add(TaskItem(taskName, false))
                adapter.notifyDataSetChanged() // Refresh the list
                inputTask.text.clear() // Clear input field
            }
        }

        // Implement search functionality
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // No action needed on text submit
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Filter as user types
                adapter.filter.filter(newText)
                return false
            }
        })

        // Back button logic
        val backButton: Button = findViewById(R.id.back_to_MainActivity)
        backButton.setOnClickListener {
            finish() // Close this activity and return to the previous one
        }
    }
}
