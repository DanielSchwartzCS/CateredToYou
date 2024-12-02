<?php
require_once 'dbcontroller.php';
require_once 'response.php';
require_once 'jwt_utils.php';

$db = new DBController();

if ($_SERVER['REQUEST_METHOD'] == 'POST' && isset($_POST['action'])) {
    $action = $_POST['action'];
    $token = $_POST['token'] ?? '';
    $payload = validateJwt($token);

    if (!$payload) {
        $db->respondWithError(401, "Unauthorized access");
    }

    switch ($action) {
        case 'createTask':
            if ($payload['role'] !== 'caterer') {
                $db->respondWithError(403, "Permission denied");
            }

            $eventId = $_POST['event_id'];
            $taskName = $_POST['task_name'];
            $assignedTo = $_POST['assigned_to'];

            if (empty($eventId) || empty($taskName) || empty($assignedTo)) {
                $db->respondWithError(400, "Event ID, task name, and assignee are required");
            }

            $stmt = $db->getConnection()->prepare(
                "INSERT INTO tasks (event_id, task_name, assigned_to) VALUES (:event_id, :task_name, :assigned_to)"
            );
            $stmt->bindValue(':event_id', $eventId);
            $stmt->bindValue(':task_name', $taskName);
            $stmt->bindValue(':assigned_to', $assignedTo);

            if ($stmt->execute()) {
                $db->respondWithSuccess(201, "Task created successfully");
            } else {
                $db->respondWithError(500, "Task creation failed");
            }
            break;

        case 'markTaskComplete':
            if ($payload['role'] !== 'employee') {
                $db->respondWithError(403, "Permission denied");
            }

            $taskId = $_POST['task_id'];

            if (empty($taskId)) {
                $db->respondWithError(400, "Task ID is required");
            }

            $stmt = $db->getConnection()->prepare(
                "UPDATE tasks SET status = 'completed' WHERE task_id = :task_id"
            );
            $stmt->bindValue(':task_id', $taskId);

            if ($stmt->execute()) {
                $db->respondWithSuccess(200, "Task marked as complete");
            } else {
                $db->respondWithError(500, "Failed to mark task as complete");
            }
            break;

        default:
            $db->respondWithError(400, "Invalid action");
    }
} else {
    $db->respondWithError(405, "Invalid request method");
}
?>
