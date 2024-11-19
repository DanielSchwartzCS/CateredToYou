<?php
include 'db_connect.php';

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $event_id = $_POST['event_id'] ?? 0;
    $inventory_items = json_decode($_POST['inventory_items'], true);

    if (!$event_id || !$inventory_items) {
        echo json_encode([
            "status" => false,
            "message" => "Missing required fields"
        ]);
        exit;
    }

    try {
        $connection->begin_transaction();

        $stmt = $connection->prepare("
            INSERT INTO event_inventory (event_id, inventory_id, quantity)
            VALUES (?, ?, ?)
        ");

        foreach ($inventory_items as $item) {
            $stmt->bind_param("iii", $event_id, $item['inventory_id'], $item['quantity']);
            if (!$stmt->execute()) {
                throw new Exception("Failed to add inventory item");
            }
        }

        $connection->commit();
        echo json_encode([
            "status" => true,
            "message" => "Event inventory added successfully"
        ]);

        $stmt->close();
    } catch (Exception $e) {
        $connection->rollback();
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