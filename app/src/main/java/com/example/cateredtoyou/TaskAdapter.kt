package com.example.cateredtoyou




import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout




class TaskAdapter(
    private val tasks: List<TaskItem>,
    private val onComplete: (Int) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>(){








    // ViewHolder class to hold views for each task item
    inner class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val taskName: TextView = view.findViewById(R.id.TaskName)
        private val taskDescription: TextView = view.findViewById(R.id.TaskDescription)
        private val dueDate: TextView = view.findViewById(R.id.DueDate)
        private val completeButton: Button = view.findViewById(R.id.btnCompleteTask)
        private val checkMark: ImageView = view.findViewById(R.id.checkmarkOverlay)
        private val taskLayout: LinearLayout = view.findViewById(R.id.taskLayout)


        fun bind(task: TaskItem){
            taskName.text = task.task_name
            taskDescription.text = task.task_description
            dueDate.text = "Due Date: ${task.due_date}"
            if(task.status == "Complete"){
                checkMark.visibility = View.VISIBLE
                taskLayout.alpha = 0.5f
                completeButton.visibility = View.GONE
            }else{
                checkMark.visibility = View.GONE
                taskLayout.alpha = 1f
                completeButton.visibility = View.VISIBLE
            }
            completeButton.setOnClickListener {onComplete(task.task_id)}




        }
    }




    // Create and return a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }




    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }




    // Return the number of items in the filtered list
    override fun getItemCount(): Int {
        return tasks.size
    }








    // Implement the filter logic for the SearchView
//    override fun getFilter(): Filter? {
//        return null
//        return object : Filter() {
//            override fun performFiltering(constraint: CharSequence?): FilterResults {
//                val charString = constraint?.toString() ?: ""
//                filteredTaskList = if (charString.isEmpty()) {
//                    taskList // No filtering, show the full list
//                } else {
//                    val filteredList = ArrayList<TaskItem>()
//                    for (task in taskList) {
//                        if (task.taskName.contains(charString, true)) {
//                            filteredList.add(task)
//                        }
//                    }
//                    filteredList
//                }
//                val filterResults = FilterResults()
//                filterResults.values = filteredTaskList
//                return filterResults
//            }
//
//            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
//                filteredTaskList = results?.values as ArrayList<TaskItem>
//                notifyDataSetChanged() // Refresh RecyclerView with filtered tasks
//            }
//        }
//    }
}
