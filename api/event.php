<?php
require_once 'dbcontroller.php';
require_once 'response.php';

function handleRequest($method, $segments) {
    $routeHandlers = [
        'GET' => 'fetchEvents',
        'POST' => 'createEvent',
        'PUT' => 'updateEvent',
        'DELETE' => 'deleteEvent'
    ];

    if (isset($routeHandlers[$method])) {
        $handlerFunction = $routeHandlers[$method];
        $handlerFunction($segments);
    } else {
        respondWithError("Method not allowed", 405);
    }
}

// Fetch events or a specific event by ID
function fetchEvents($segments) {
    $eventId = $segments[0] ?? null;

    try {
        if ($eventId) {
            $query = "SELECT * FROM events WHERE event_id = :event_id";
            $params = [':event_id' => $eventId];
            $result = executeSelect($query, $params);

            if (!$result) {
                respondWithError("Event not found", 404);
            } else {
                respondWithSuccess("Event retrieved successfully", 200, $result[0]);
            }
        } else {
            $query = "SELECT * FROM events";
            $result = executeSelect($query);

            respondWithSuccess("Events retrieved successfully", 200, $result);
        }
    } catch (Exception $e) {
        respondWithError("Failed to fetch event(s): " . $e->getMessage(), 500);
    }
}

// Create a new event
function createEvent() {
    $data = json_decode(file_get_contents("php://input"), true);

    // Required fields
    $requiredFields = ['event_description', 'event_date', 'event_time', 'location', 'num_guests'];
    foreach ($requiredFields as $field) {
        if (empty($data[$field])) {
            respondWithError("$field is required", 400);
        }
    }

    try {
        $query = "INSERT INTO events (event_description, event_date, event_time, location, num_guests, notes)
                  VALUES (:event_description, :event_date, :event_time, :location, :num_guests, :notes)";
        $params = [
            ':event_description' => $data['event_description'],
            ':event_date' => $data['event_date'],
            ':event_time' => $data['event_time'],
            ':location' => $data['location'],
            ':num_guests' => $data['num_guests'],
            ':notes' => $data['notes'] ?? null
        ];

        $rowCount = executeChange($query, $params);

        if ($rowCount > 0) {
            respondWithSuccess("Event created successfully", 201);
        } else {
            respondWithError("Failed to create event", 500);
        }
    } catch (Exception $e) {
        respondWithError("Failed to create event: " . $e->getMessage(), 500);
    }
}

// Update an existing event
function updateEvent($segments) {
    $eventId = $segments[0] ?? null;

    if (!$eventId) {
        respondWithError("Event ID is required for updating", 400);
    }

    $data = json_decode(file_get_contents("php://input"), true);

    try {
        $fieldsToUpdate = [];
        $params = [':event_id' => $eventId];

        // Add fields dynamically
        foreach ($data as $field => $value) {
            $fieldsToUpdate[] = "$field = :$field";
            $params[":$field"] = $value;
        }

        if (empty($fieldsToUpdate)) {
            respondWithError("No valid fields provided to update", 400);
        }

        $query = "UPDATE events SET " . implode(', ', $fieldsToUpdate) . " WHERE event_id = :event_id";
        $rowCount = executeChange($query, $params);

        if ($rowCount > 0) {
            respondWithSuccess("Event updated successfully", 200);
        } else {
            respondWithError("Event not found or no changes made", 404);
        }
    } catch (Exception $e) {
        respondWithError("Failed to update event: " . $e->getMessage(), 500);
    }
}

// Delete an event
function deleteEvent($segments) {
    $eventId = $segments[0] ?? null;

    if (!$eventId) {
        respondWithError("Event ID is required for deletion", 400);
    }

    try {
        $query = "DELETE FROM events WHERE event_id = :event_id";
        $params = [':event_id' => $eventId];

        $rowCount = executeChange($query, $params);

        if ($rowCount > 0) {
            respondWithSuccess("Event deleted successfully", 200);
        } else {
            respondWithError("Event not found", 404);
        }
    } catch (Exception $e) {
        respondWithError("Failed to delete event: " . $e->getMessage(), 500);
    }
}
?>
