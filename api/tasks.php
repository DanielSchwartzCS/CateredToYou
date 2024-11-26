<?php
require_once 'dbcontroller.php';
require_once 'response.php';

function handleRequest($method, $segments) {
    if ($method !== 'GET') {
        respondWithError("Method not allowed", 405);
    }

    // Map routes to handlers
    $routeHandlers = [
        '' => 'fetchTasks' // Default route handles tasks fetching
    ];

    $route = implode('/', $segments);

    if (isset($routeHandlers[$route])) {
        $handlerFunction = $routeHandlers[$route];
        $handlerFunction();
    } else {
        respondWithError("Route not found", 404);
    }
}

function fetchTasks() {
    $prep_window = $_GET['prep_window'] ?? null;

    try {
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
    } catch (Exception $e) {
        respondWithError("Failed to fetch tasks: " . $e->getMessage(), 500);
    }
}
?>