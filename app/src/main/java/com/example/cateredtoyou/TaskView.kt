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
import com.example.cateredtoyou.apifiles.DatabaseApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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


        // Handle the back button to return to the previous activity (Dashboard)
        val backButton: Button = findViewById(R.id.back_to_MainActivity)
        backButton.setOnClickListener {
            finish() // Close the current activity and return to Dashboard
        }


        // Initialize views
        taskRecyclerView = findViewById(R.id.todo_list_view)
        searchView = findViewById(R.id.search_view)


        // Initialize RecyclerView and Adapter
        adapter = TaskAdapter(taskList, this::onComplete)
        taskRecyclerView.adapter = adapter
        taskRecyclerView.layoutManager = LinearLayoutManager(this)


        findViewById<Button>(R.id.btnToday).setOnClickListener {fetchTasks("today")}
        findViewById<Button>(R.id.btn3Days).setOnClickListener {fetchTasks("within3days")}
        findViewById<Button>(R.id.btnBeyond3Days).setOnClickListener {fetchTasks("beyond3days")}

        fetchTasks("today")

//        // SearchView logic to filter the task list as the user types
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                return false
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                adapter.filter.filter(newText) // Use adapter's filter method
//                return false
//            }
//        })

    }


    private fun fetchTasks(filter: String) {
        DatabaseApi.retrofitService.getTasks(filter).enqueue(object : Callback<List<TaskItem>> {
            override fun onResponse(call: Call<List<TaskItem>>, response: Response<List<TaskItem>>) {
                if (response.isSuccessful){
                    taskList.clear()
                    taskList.addAll(response.body() ?: emptyList())
                    adapter.notifyDataSetChanged()
                }else{
                    Toast.makeText(this@TaskView, "Failed to load tasks", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<TaskItem>>, t: Throwable) {
                Toast.makeText(this@TaskView, "Failed to load tasks", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun onComplete(id: Int){
        DatabaseApi.retrofitService.deleteTasks(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // Remove the task locally
                    taskList.removeAll { it.id == id }
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this@TaskView, "Task completed", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@TaskView, "Failed to complete task", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@TaskView, "Failed to complete task", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

