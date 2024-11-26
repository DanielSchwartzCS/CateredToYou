package com.example.cateredtoyou


import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var searchView: SearchView
    private val taskList = ArrayList<TaskItem>()
    private val allTaskList = ArrayList<TaskItem>()
    private var filteredTaskList: MutableList<TaskItem> = mutableListOf()


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
        adapter = TaskAdapter(filteredTaskList, this::onComplete)
        taskRecyclerView.adapter = adapter
        taskRecyclerView.layoutManager = LinearLayoutManager(this)


        // SearchView logic to filter the task list as the user types
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterTasks(query)
                return false
            }


            override fun onQueryTextChange(newText: String?): Boolean {
                filterTasks(newText) // Use adapter's filter method
                return false
            }
        })



        findViewById<Button>(R.id.btnToday).setOnClickListener{ fetchTasks("today")}
        findViewById<Button>(R.id.btnThisWeek).setOnClickListener{ fetchTasks("this_week")}
        findViewById<Button>(R.id.btnAllTasks).setOnClickListener{ fetchTasks("all_tasks")}


        fetchTasks("today")




    }


    private fun fetchTasks(filter: String) {
        DatabaseApi.retrofitService.getTasks(filter).enqueue(object : Callback<List<TaskItem>> {
            override fun onResponse(call: Call<List<TaskItem>>, response: Response<List<TaskItem>>) {
                if (response.isSuccessful) {
                    taskList.clear()
                    taskList.addAll(response.body() ?: emptyList())
                    filterTasks("") // Populate filteredTasksList with the full data
                } else {
                    Toast.makeText(this@TaskView, "Failed to load tasks", Toast.LENGTH_SHORT).show()
                }
            }


            override fun onFailure(call: Call<List<TaskItem>>, t: Throwable) {
                Log.e("API_ERROR", t.toString())
                Toast.makeText(this@TaskView, "Completely Failed to load tasks", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun filterTasks(query: String?){
        filteredTaskList.clear()
        if(query.isNullOrEmpty()){
            filteredTaskList.addAll(taskList)
        }else{
            val searchText = query.lowercase()
            filteredTaskList.addAll(taskList.filter{
                it.task_name.lowercase().contains(searchText)
            })
        }
        adapter.notifyDataSetChanged()
    }


    private fun onComplete(taskId: Int){
        DatabaseApi.retrofitService.updateTasks(taskId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    taskList.find { it.task_id == taskId }?.status = "Complete"
                    filterTasks("") // Reapply filter to reflect changes
                    Toast.makeText(this@TaskView, "Task marked as complete", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@TaskView, "Failed to update task status", Toast.LENGTH_SHORT).show()
                }
            }


            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@TaskView, "Failed to update task status", Toast.LENGTH_SHORT).show()
            }
        })
    }


}

