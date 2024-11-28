<?php
require_once 'dbcontroller.php';
require_once 'response.php';

// Fetch all tasks or tasks filtered by prep_window
function fetchTasks($segments) {
    $prep_window = $_GET['prep_window'] ?? null;

    if ($prep_window && is_numeric($prep_window) && $prep_window > 0) {
        $response = executeSelect(
            "SELECT * FROM tasks
            WHERE due_date BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL :prep_window DAY)",
            [':prep_window' => $prep_window]);
        respondWithSuccess("Tasks within prep window retrieved", 200, $response);
    } else {
        $response = executeSelect("SELECT * FROM tasks");
        respondWithSuccess("All tasks retrieved", 200, $response);
    }
}

// Fetch tasks by event_id (event is in segments[1])
function fetchTasksByEvent($segments) {
    $event_id = $segments[1] ?? null;

    if ($event_id && is_numeric($event_id)) {
        $response = executeSelect(
            "SELECT * FROM tasks WHERE event_id = :event_id",
            [':event_id' => $event_id]
        );
        respondWithSuccess("Tasks for event $event_id retrieved", 200, $response);
    } else {
        respondWithError("Event ID is required and should be numeric", 400);
    }
}

// Update status of a task
function updateStatus($segments) {
    $task_id = $segments[0];
    $inputData = json_decode(file_get_contents('php://input'), true);
    $status = $inputData['status'] ?? null;

    if (!$status) {
        respondWithError("Status is required", 400);
    }

    $currentStatus = executeSelect("SELECT status FROM tasks WHERE task_id = :task_id",
        [':task_id' => $task_id], false);
    if (!$currentStatus) {
        respondWithError("Task $task_id not found", 400);
    }
    if ($status === $currentStatus) {
        respondWithError("Status must be different than original status to update it.", 400);
    }

    if (!executeChange("UPDATE tasks SET status = :status WHERE task_id = :task_id",
        [':task_id' => $task_id, ':status' => $status])) {
        respondWithError("Failed to update status", 404);
    }

    respondWithSuccess("Task $task_id status updated to $status", 200);
}

// Create a new task (PUT method)
function createTask($segments) {
    $inputData = json_decode(file_get_contents('php://input'), true);

    if (empty($inputData['task_description']) || empty($inputData['status']) ||
        empty($inputData['event_id']) || empty($inputData['department_id']) ||
        empty($inputData['due_date'])) {
        respondWithError("Required fields are missing", 400);
    }

    if (!executeChange(
        "INSERT INTO tasks (event_id, department_id, task_description, status, due_date)
         VALUES (:event_id, :department_id, :task_description, :status, :due_date)",
        [
            ':event_id' => $inputData['event_id'],
            ':department_id' => $inputData['department_id'],
            ':task_description' => $inputData['task_description'],
            ':status' => $inputData['status'],
            ':due_date' => $inputData['due_date']
        ]
    )) {
        respondWithError("Failed to create task", 500);
    }
}
?>
