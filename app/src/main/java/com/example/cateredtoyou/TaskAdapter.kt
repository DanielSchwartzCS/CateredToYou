package com.example.cateredtoyou

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.widget.Filter
import android.widget.Filterable

class TaskAdapter(private val context: Context, private var taskList: ArrayList<TaskItem>) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>(), Filterable {

    private var filteredTaskList: ArrayList<TaskItem> = taskList // Copy of task list for filtering

    // ViewHolder class to hold views for each task item
    inner class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskText: TextView = view.findViewById(R.id.task_text)
        val checkBox: CheckBox = view.findViewById(R.id.task_checkbox)
    }

    // Create and return a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_task_item, parent, false)
        return TaskViewHolder(view)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val taskItem = filteredTaskList[position] // Use filtered list

        // Set task name
        holder.taskText.text = taskItem.taskName

        // Reset the listener to prevent unwanted triggers
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = taskItem.isCompleted

        // Apply strikethrough if task is completed
        if (taskItem.isCompleted) {
            holder.taskText.paintFlags = holder.taskText.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            holder.taskText.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
        } else {
            holder.taskText.paintFlags = holder.taskText.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.taskText.setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }

        // Set listener for checkbox after resetting
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            taskItem.isCompleted = isChecked
            notifyItemChanged(position) // Refresh only this item instead of the whole dataset
        }
    }

    // Return the number of items in the filtered list
    override fun getItemCount(): Int {
        return filteredTaskList.size
    }

    // Implement the filter logic for the SearchView
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString() ?: ""
                filteredTaskList = if (charString.isEmpty()) {
                    taskList // No filtering, show the full list
                } else {
                    val filteredList = ArrayList<TaskItem>()
                    for (task in taskList) {
                        if (task.taskName.contains(charString, true)) {
                            filteredList.add(task)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredTaskList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredTaskList = results?.values as ArrayList<TaskItem>
                notifyDataSetChanged() // Refresh RecyclerView with filtered tasks
            }
        }
    }
}
