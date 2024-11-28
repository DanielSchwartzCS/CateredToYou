<?php
require_once 'dbcontroller.php';
require_once 'response.php';

// Fetch events or a specific event by ID. Be cautious, with a large scale
// caterer this could be a huge amount of data if fetching all events
function fetchEvents($segments) {
    if ($segments[0] === '') { //fetch all events
        respondWithSuccess("Events retrieved successfully", 200,
            executeSelect("SELECT * FROM events")
        );
    } else {
        $event_id = validateResource($segments, 0);
        $result = executeSelect("SELECT * FROM events WHERE event_id = :event_id",
        [':event_id' => $eventId], false);

        if (!$result) {
            respondWithError("Event not found", 204);
        } else {
            respondWithSuccess("Event retrieved successfully", 200, $result);
        }
    }
}

// Create a new event
function createEvent() {
    $fieldsAndTypes = [
        'event_description' => 'string',
        'event_date' => 'date',
        'event_time' => 'time',
        'location' => 'string',
        'num_guests' => 'nonNegInt'
    ];

    $data = validateBody($fieldsAndTypes);

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

    if (!executeChange($query, $params)) {
        respondWithError("Failed to create event", 500);
    }
}

// Update an existing event
function updateEvent($segments) {
    $eventId = validateResource($segments, 0, 'posInt');

    $fieldsAndTypes = [
        'event_description' => 'string',
        'event_date' => 'date',
        'event_time' => 'time',
        'location' => 'string',
        'num_guests' => 'posInt',
    ];
    $data = validateBody($fieldsAndTypes);

    // Prepare the SQL query to update the event
    $query = "UPDATE events
              SET event_description = :event_description,
                  event_date = :event_date,
                  event_time = :event_time,
                  location = :location,
                  num_guests = :num_guests,
                  notes = :notes
              WHERE event_id = :event_id";

    // Prepare the parameters for the query
    $params = [
        ':event_id' => $eventId,
        ':event_description' => $data['event_description'],
        ':event_date' => $data['event_date'],
        ':event_time' => $data['event_time'],
        ':location' => $data['location'],
        ':num_guests' => $data['num_guests'],
        ':notes' => $data['notes'] ?? null
    ];

    if (!executeChange($query, $params)) {
        respondWithError("Event not found or no changes made", 404);
    }
}

// Delete an event. Should likely never be used. TODO: implement archive
function deleteEvent($segments) {
    $eventId = validateResource($segments, 0, 'posInt');

    if (!executeChange("DELETE FROM events WHERE event_id = :event_id",
        [':event_id' => $eventId]
    )) {
        respondWithError("Event not deleted", 204);
    }
}
?>
