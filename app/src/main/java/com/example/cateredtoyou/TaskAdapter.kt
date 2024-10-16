package com.example.cateredtoyou

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat

class TaskAdapter(private val context: Context, private var taskList: ArrayList<TaskItem>) : BaseAdapter(), Filterable {

    private var filteredTaskList: ArrayList<TaskItem> = taskList

    override fun getCount(): Int {
        return filteredTaskList.size
    }

    override fun getItem(position: Int): Any {
        return filteredTaskList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.activity_task_item, parent, false)

        // Get views from the task_item.xml layout
        val checkBox: CheckBox = view.findViewById(R.id.task_checkbox)
        val taskText: TextView = view.findViewById(R.id.task_text)
        val deleteButton: ImageButton = view.findViewById(R.id.delete_button)

        // Get the task for the current position
        val taskItem = filteredTaskList[position]

        // Set task name and checkbox state
        taskText.text = taskItem.taskName
        checkBox.isChecked = taskItem.isCompleted

        // Apply strikethrough if task is completed
        if (taskItem.isCompleted) {
            taskText.paintFlags = taskText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            taskText.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
        } else {
            taskText.paintFlags = taskText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            taskText.setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }

        // Handle checkbox change
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            taskItem.isCompleted = isChecked
            notifyDataSetChanged()  // Refresh the list to reflect completion status
        }

        // Handle delete button click
        deleteButton.setOnClickListener {
            taskList.remove(taskItem)
            notifyDataSetChanged()  // Refresh the list after deletion
        }

        return view
    }

    // Filtering logic for the SearchView
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString() ?: ""
                filteredTaskList = if (charString.isEmpty()) {
                    taskList
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
                notifyDataSetChanged()  // Refresh the list after filtering
            }
        }
    }
}
