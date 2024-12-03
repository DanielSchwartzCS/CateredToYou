<?php
// api/resources/task.php
require_once __DIR__ . '/../database/dbcontroller.php';
require_once __DIR__ . '/../utils/response.php';
require_once __DIR__ . '/../data-processors/input.php';

// Fetch all tasks
function fetchTasks() {
    $tasks = executeSelect("SELECT * FROM tasks");

    if (empty($tasks)) {
        respondSuccess(null, 204);
    }

    respondSuccess($tasks);
}

// Fetch tasks by event ID
function fetchTasksByEvent($eventId) {
    $tasks = executeSelect(
        "SELECT * FROM tasks WHERE event_id = :event_id",
        [':event_id' => $eventId]
    );

    if (empty($tasks)) {
        respondSuccess("No tasks found for the specified event", 204);
    }

    respondSuccess($tasks);
}

// Create a new task
function createTask($taskId) {
    $requiredFields = [
        'event_id' => 'posInt',
        'department_id' => 'posInt',
        'due_date' => 'date',
        'task_description' => 'string',
        'status' => 'string'
    ];

    $data = getValidHttpBody($requiredFields)[0];

    $newTaskId = executeInsert(
        "INSERT INTO tasks (event_id, department_id, due_date, task_description, status)
         VALUES (:event_id, :department_id, :due_date, :task_description, :status)",
        $data
    );

    respondSuccess(['task_id' => $newTaskId], 201);
}

// Update the task status
function updateStatus($taskId) {
    $requiredFields = [
        'status' => 'string'
    ];

    $data = getValidHttpBody($requiredFields)[0];

    $result = executeChange(
        "UPDATE tasks SET status = :status WHERE task_id = :task_id",
        array_merge($data, ['task_id' => $taskId])
    );

    respondSuccess(null);
}
?>
