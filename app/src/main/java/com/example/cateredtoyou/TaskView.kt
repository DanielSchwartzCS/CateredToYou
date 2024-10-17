package com.example.cateredtoyou

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.SearchView
import android.widget.Toast

class TaskView : AppCompatActivity() {

    private lateinit var taskRecyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private lateinit var inputTask: EditText
    private lateinit var addTaskButton: Button
    private lateinit var searchView: SearchView
    private val taskList = ArrayList<TaskItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_taskview)

        // Initialize views
        taskRecyclerView = findViewById(R.id.todo_list_view)
        inputTask = findViewById(R.id.input_task)
        addTaskButton = findViewById(R.id.add_task_button)
        searchView = findViewById(R.id.search_view)

        // Initialize RecyclerView and Adapter
        adapter = TaskAdapter(this, taskList)
        taskRecyclerView.adapter = adapter
        taskRecyclerView.layoutManager = LinearLayoutManager(this)

        // Add Task Button click listener
        addTaskButton.setOnClickListener {
            val taskName = inputTask.text.toString().trim() // Remove leading and trailing whitespace

            // Validate and sanitize the task name
            if (taskName.isNotEmpty() && isValidTaskName(taskName)) {
                val sanitizedTaskName = sanitizeInput(taskName)
                taskList.add(TaskItem(sanitizedTaskName, false)) // Add sanitized task
                adapter.notifyDataSetChanged()
                inputTask.text.clear() // Clear input field after adding the task
            } else {
                inputTask.error = "Invalid task name. Please use only letters, numbers, and basic punctuation."
            }
        }

        // SearchView logic to filter the task list as the user types
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText) // Use adapter's filter method
                return false
            }
        })

        // Add swipe-to-delete functionality using ItemTouchHelper
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                taskList.removeAt(position)
                adapter.notifyItemRemoved(position)
            }
        }

        // Attach the ItemTouchHelper to RecyclerView
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(taskRecyclerView)
    }

    // Function to validate the task name
    private fun isValidTaskName(taskName: String): Boolean {
        // Only allow letters, numbers, spaces, and common punctuation marks
        val regex = Regex("^[a-zA-Z0-9\\s.,!?'-]*$")
        return regex.matches(taskName)
    }

    // Function to sanitize input
    private fun sanitizeInput(input: String): String {
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
    }
}
