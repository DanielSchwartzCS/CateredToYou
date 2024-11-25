package com.example.cateredtoyou


data class TaskItem(
    val task_id: Int,
    val event_id: Int,
    val department_id: Int,
    val task_name: String,
    val task_description: String,
    val due_date: String,
    var status: String
)
