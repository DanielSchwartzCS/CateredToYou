<?php

ini_set('display_errors', 1);
error_reporting(E_ALL);

//  database connection
require_once 'db_connect.php';

// Set headers
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

// Debug log
error_log("Received request at add_event.php");

try {
    // Get JSON input
    $json_data = file_get_contents('php://input');
    error_log("Received data: " . $json_data);
    
    // Decode JSON
    $data = json_decode($json_data, true);
    
    // Check if JSON is valid
    if ($data === null) {
        throw new Exception("Invalid JSON data");
    }
    
    // Extract data
    $name = $data['name'] ?? '';
    $event_date = $data['eventDate'] ?? '';
    $start_time = $data['eventStartTime'] ?? '';
    $end_time = $data['eventEndTime'] ?? '';
    $location = $data['location'] ?? '';
    $status = strtolower($data['status'] ?? 'pending');
    $guests = $data['numberOfGuests'] ?? 0;
    $client_id = $data['clientId'] ?? 0;
    $employee_id = $data['employeeId'] ?? 0;
    $additional_info = $data['additionalInfo'] ?? '';

    // Validate required fields
    if (empty($name) || empty($event_date) || empty($start_time) || 
        empty($end_time) || empty($location)) {
        throw new Exception("Please fill all required fields");
    }

    // Convert date format if needed (DD/MM/YYYY to YYYY-MM-DD)
    $date_parts = explode('/', $event_date);
    if (count($date_parts) === 3) {
        $event_date = "{$date_parts[2]}-{$date_parts[1]}-{$date_parts[0]}";
    }

    // Validate status
    $valid_statuses = ['pending', 'confirmed', 'completed', 'canceled'];
    if (!in_array($status, $valid_statuses)) {
        $status = 'pending';
    }

    // Prepare SQL statement
    $sql = "INSERT INTO events (
        name, event_date, event_start_time, event_end_time, location,
        status, number_of_guests, client_id, employee_id, additional_info
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    // Prepare and bind parameters
    $stmt = $connection->prepare($sql);
    if (!$stmt) {
        throw new Exception("Database preparation failed: " . $connection->error);
    }

    $stmt->bind_param("ssssssiiis",
        $name,
        $event_date,
        $start_time,
        $end_time,
        $location,
        $status,
        $guests,
        $client_id,
        $employee_id,
        $additional_info
    );

    // Execute the statement
    if (!$stmt->execute()) {
        throw new Exception("Failed to create event: " . $stmt->error);
    }

    // Get the new event ID
    $new_event_id = $connection->insert_id;

    // Return success response
    echo json_encode([
        'success' => true,
        'message' => 'Event created successfully',
        'id' => $new_event_id
    ]);

} catch (Exception $e) {
    error_log("Error in add_event.php: " . $e->getMessage());
    
    // Return error response
    echo json_encode([
        'success' => false,
        'message' => $e->getMessage()
    ]);
    
} finally {
    
    if (isset($stmt)) {
        $stmt->close();
    }
    if (isset($connection)) {
        $connection->close();
    }
}
?>