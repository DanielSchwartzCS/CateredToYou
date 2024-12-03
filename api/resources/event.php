<?php
// api/resources/event.php
require_once __DIR__ . '/../database/dbcontroller.php';
require_once __DIR__ . '/../utils/response.php';
require_once __DIR__ . '/../data-processors/input.php';

// Fetch all events
function fetchAllEvents() {
    $events = executeSelect("SELECT * FROM events");

    if (empty($events)){
        respondSuccess(null, 204);
    }

    respondSuccess($events);
}

function fetchEventById($eventId) {
    $event = executeSelect(
        "SELECT * FROM events WHERE event_id = :event_id",
        [':event_id' => $eventId],
        false
    );

    if (!$event) {
        respondError("Event not found", 404);
    }

    respondSuccess($event);
}

function fetchClientEvents($clientId) {

    $events = executeSelect(
        "SELECT * FROM events WHERE client_id = :client_id",
        [':client_id' => $clientId]
    );

    if (empty($events)) {
        respondSuccess("Client $clientId has no events.", 204);
    }

    respondSuccess($events);
}


function createEvent() {
    $requiredFields = [
        'event_description' => 'string',
        'event_date' => 'date',
        'event_time' => 'time',
        'location' => 'string',
        'num_guests' => 'nonNegInt',
        'notes' => 'string'
    ];

    $data = getValidHttpBody($requiredFields)[0];

    $eventId = executeInsert(
        "INSERT INTO events (
            event_description,
            event_date,
            event_time,
            location,
            num_guests,
            notes
        ) VALUES (
            :event_description,
            :event_date,
            :event_time,
            :location,
            :num_guests,
            :notes
        )",
        $data
    );

    respondSuccess(['event_id' => $eventId], 201);
}

function updateEvent($eventId) {

    $updateFields = [
        'event_description' => 'string',
        'event_date' => 'date',
        'event_time' => 'time',
        'location' => 'string',
        'num_guests' => 'posInt',
        'notes' => 'string'
    ];

    $data = getValidHttpBody($updateFields)[0];

    $result = executeChange(
        "UPDATE events SET
        event_description = :event_description,
        event_date = :event_date,
        event_time = :event_time,
        location = :location,
        num_guests = :num_guests,
        notes = :notes
        WHERE event_id = :event_id",
        ['event_id' => $eventId]
    );

    respondSuccess(null);
}
?>