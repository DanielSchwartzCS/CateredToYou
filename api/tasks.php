<?php
require_once 'dbcontroller.php';
require_once 'response.php';

function handleRequest($method, $segments) {
    if ($method !== 'GET') {
        respondWithError("Method not allowed", 405);
    }

    // Map routes to their handlers
    $routeHandlers = [
        '' => 'fetchTasks' // Default route fetches tasks
    ];

    $route = implode('/', $segments);

    $queryString = parse_url($_SERVER['REQUEST_URI'], PHP_URL_QUERY);
    parse_str($queryString, $params);

    if (!isset($params['filter'])) {
        respondWithError(400, 'Error: Missing required parameters "filter".');
    }
    var_dump($params);


    if (isset($routeHandlers[$route])) {
        $handlerFunction = $routeHandlers[$route];
        $handlerFunction();
    } else {
        respondWithError("Route not found", 404);
    }
}

function fetchTasks() {
    $filter = $params['filter'];

    // Build the query based on the filter
    $query = "";
    if ($filter === 'today') {
        $query = "SELECT * FROM tasks WHERE due_date = CURDATE()";
    } elseif ($filter === 'this_week') {
        $query = "SELECT * FROM tasks WHERE YEARWEEK(due_date, 1) = YEARWEEK(CURDATE(), 1)";
    } elseif ($filter === 'all_tasks') {
        $query = "SELECT * FROM tasks";
    } else {
        respondWithError("Invalid filter parameter", 400);
        return;
    }

    try {
        // Fetch tasks from the database
        $tasks = executeSelect($query);
        respondWithSuccess(200, "Tasks retrieved successfully", $tasks);
    } catch (Exception $e) {
        respondWithError("Failed to fetch tasks: " . $e->getMessage(), 500);
    }
}

// Entry point
handleRequest($_SERVER['REQUEST_METHOD'], explode('/', trim($_SERVER['REQUEST_URI'], '/')));
