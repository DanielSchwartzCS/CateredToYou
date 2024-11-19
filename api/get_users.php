<?php
include 'db_connect.php';

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] == 'GET') {
    try {
        $stmt = $connection->prepare("SELECT id, username, role, first_name, last_name FROM users");
        $stmt->execute();
        $result = $stmt->get_result();
        
        $users = array();
        while ($row = $result->fetch_assoc()) {
            $users[] = array(
                'id' => (int)$row['id'],
                'username' => $row['username'],
                'role' => $row['role'],
                'first_name' => $row['first_name'],
                'last_name' => $row['last_name']
            );
        }
        
        echo json_encode($users);
        $stmt->close();
    } catch (Exception $e) {
        http_response_code(500);
        echo json_encode([
            "status" => false,
            "message" => "Server error: " . $e->getMessage()
        ]);
    }
} else {
    http_response_code(405);
    echo json_encode([
        "status" => false,
        "message" => "Invalid request method"
    ]);
}

$connection->close();
?>