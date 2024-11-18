<?php
require_once 'dbcontroller.php';
require_once 'response.php';
require_once 'jwt.php';
require_once 'auth.php';


//TODO: make sure we are SELECTing the right columns


function executeQuery($query, $params = [], $fetchMethod = PDO::FETCH_ASSOC) {
    $db = new DBController();
    $stmt = $db->conn->prepare($query);
    $stmt->execute($params);
    return $fetchMethod == PDO::FETCH_ASSOC
        ? $stmt->fetchAll(PDO::FETCH_ASSOC)
        : $stmt->fetch(PDO::FETCH_ASSOC);
}

function handleRequest($method, $segments) {
    $authHeader = $_SERVER['HTTP_AUTHORIZATION'] ?? null;
    $userData = validateAuthorization($authHeader);

    if (!$userData) {
        respondWithError("Authorization header missing or malformed", 401);
    }

    $userRole = $userData->role;
    if ($userRole !== 'caterer') {
        respondWithError("Unauthorized access", 403);
    }

    $routeHandlers = [
        'GET' => [
            'upcoming' => 'getUpcomingEvents',
            '{event_id}' => 'getEventDetails',
            'archive' => 'getArchivedEvents',
            'date-range' => 'getEventsByDateRange',
            '{event_id}/location' => 'getEventLocation',
            'upcoming/locations' => 'getUpcomingEventLocations'
        ]
    ];

    if (isset($routeHandlers[$method])) {
        $route = implode('/', $segments);
        if (isset($routeHandlers[$method][$route])) {
            $handlerFunction = $routeHandlers[$method][$route];
            $handlerFunction($segments);
        } else {
            respondWithError("Route not found", 404);
        }
    } else {
        respondWithError("Method not allowed", 405);
    }
}

function getUpcomingEvents($segments) {
    respondWithSuccess(200, "Upcoming events retrieved successfully", executeQuery(
        "SELECT event_id, event_description, start_date, location, event_status
         FROM Events WHERE start_date >= CURRENT_DATE AND event_status != 'Canceled'"
    ));
}

function getEventDetails($segments) {
    $event_id = $segments[1];
    if (!is_numeric($event_id)) {
        respondWithError("Invalid event ID", 400);
    }

    $eventDetails = executeQuery(
        "SELECT * FROM Events WHERE event_id = :event_id", [':event_id' => $event_id]
    );
    if ($eventDetails) {
        respondWithSuccess(200, "Event details retrieved successfully", $eventDetails);
    } else {
        respondWithError("Event not found", 404);
    }
}

function getArchivedEvents($segments) {
    // Placeholder function for fetching archived events
    respondWithError("Archived events functionality not implemented", 501);
}

function getEventsByDateRange($segments) {
    $start_date = $_GET['start_date'] ?? '';
    $end_date = $_GET['end_date'] ?? '';

    if (empty($start_date) || empty($end_date)) {
        respondWithError("Start and end dates are required", 400);
    }

    respondWithSuccess(200, "Events within date range retrieved successfully", executeQuery(
        "SELECT event_id, event_description, start_date, end_date, location
         FROM Events WHERE start_date BETWEEN :start_date AND :end_date",
         [':start_date' => $start_date, ':end_date' => $end_date]
    ));
}

function getEventLocation($segments) {
    $event_id = $segments[1];
    if (!is_numeric($event_id)) {
        respondWithError("Invalid event ID", 400);
    }

    $location = executeQuery(
        "SELECT location FROM Events WHERE event_id = :event_id", [':event_id' => $event_id]
    );
    if ($location) {
        respondWithSuccess(200, "Event location retrieved successfully", $location);
    } else {
        respondWithError("Event not found", 404);
    }
}

function getUpcomingEventLocations($segments) {
    respondWithSuccess(200, "Locations for all upcoming events retrieved successfully", executeQuery(
        "SELECT event_id, location FROM Events WHERE start_date >= CURRENT_DATE AND event_status != 'Canceled'"
    ));
}

?>