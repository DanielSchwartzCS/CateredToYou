<?php
include 'db_connect.php';

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] == 'GET') {
    try {
        // Join with clients table to get client information
        $stmt = $connection->prepare("
            SELECT e.*,
                   c.firstname, c.lastname, c.email, c.phonenumber
            FROM events e
            LEFT JOIN clients c ON e.client_id = c.id
            ORDER BY e.event_date DESC
        ");

        if (!$stmt) {
            throw new Exception("Prepare failed: " . $connection->error);
        }

        if (!$stmt->execute()) {
            throw new Exception("Execute failed: " . $stmt->error);
        }

        $result = $stmt->get_result();
        $events = array();

        while ($row = $result->fetch_assoc()) {
            // Format the client data
            $client = array(
                'id' => (int)$row['client_id'],
                'firstname' => $row['firstname'],
                'lastname' => $row['lastname'],
                'email' => $row['email'],
                'phonenumber' => $row['phonenumber']
            );

            // Format the event data
            $event = array(
                'id' => (int)$row['id'],
                'name' => $row['name'],
                'event_date' => $row['event_date'],
                'event_start_time' => $row['event_start_time'],
                'event_end_time' => $row['event_end_time'],
                'location' => $row['location'],
                'status' => $row['status'],
                'number_of_guests' => (int)$row['number_of_guests'],
                'client' => $client,
                'additional_info' => $row['additional_info']
            );

            $events[] = $event;
        }

        echo json_encode([
            "status" => true,
            "events" => $events
        ]);

        $stmt->close();
    } catch (Exception $e) {
        error_log("Error getting events: " . $e->getMessage());
        echo json_encode([
            "status" => false,
            "message" => "Server error: " . $e->getMessage()
        ]);
    }
} else {
    echo json_encode([
        "status" => false,
        "message" => "Invalid request method"
    ]);
}

$connection->close();
?>