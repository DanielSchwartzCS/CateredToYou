<?php
include 'db_connect.php';

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    // Get all required fields
    $name = $_POST['name'] ?? '';
    $event_date = $_POST['event_date'] ?? '';
    $event_start_time = $_POST['event_start_time'] ?? '';
    $event_end_time = $_POST['event_end_time'] ?? '';
    $location = $_POST['location'] ?? '';
    $status = $_POST['status'] ?? 'pending';
    $number_of_guests = intval($_POST['number_of_guests'] ?? 0);
    $client_id = intval($_POST['client_id'] ?? 0);
    $employee_id = intval($_POST['employee_id'] ?? 0);
    $additional_info = $_POST['additional_info'] ?? '';

    // Validate required fields
    if (empty($name) || empty($event_date) || empty($event_start_time) || 
        empty($event_end_time) || empty($location) || $client_id === 0 || $employee_id === 0) {
        echo json_encode([
            "status" => false, 
            "message" => "Required fields are missing"
        ]);
        exit;
    }

    try {
        // First verify the employee exists
        $stmt = $connection->prepare("SELECT id FROM users WHERE id = ?");
        $stmt->bind_param("i", $employee_id);
        $stmt->execute();
        $result = $stmt->get_result();
        
        if ($result->num_rows === 0) {
            echo json_encode([
                "status" => false,
                "message" => "Invalid employee ID"
            ]);
            exit;
        }
        $stmt->close();

        // Then verify the client exists
        $stmt = $connection->prepare("SELECT id FROM clients WHERE id = ?");
        $stmt->bind_param("i", $client_id);
        $stmt->execute();
        $result = $stmt->get_result();
        
        if ($result->num_rows === 0) {
            echo json_encode([
                "status" => false,
                "message" => "Invalid client ID"
            ]);
            exit;
        }
        $stmt->close();

        // Now insert the event
        $stmt = $connection->prepare(
            "INSERT INTO events (
                name, event_date, event_start_time, event_end_time, 
                location, status, number_of_guests, client_id, 
                employee_id, additional_info
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        );

        $stmt->bind_param(
            "ssssssiiis",
            $name,
            $event_date,
            $event_start_time,
            $event_end_time,
            $location,
            $status,
            $number_of_guests,
            $client_id,
            $employee_id,
            $additional_info
        );

        if ($stmt->execute()) {
            echo json_encode([
                "status" => true,
                "message" => "Event created successfully",
                "event_id" => $connection->insert_id
            ]);
        } else {
            throw new Exception("Failed to create event: " . $stmt->error);
        }

        $stmt->close();
    } catch (Exception $e) {
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