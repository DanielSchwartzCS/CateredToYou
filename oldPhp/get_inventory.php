<?php
include 'db_connect.php';

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] == 'GET') {
    try {
        $stmt = $connection->prepare("
            SELECT id, item_name, quantity, category,
                   unit_of_measurement, cost_per_unit,
                   minimum_stock, notes, last_restocked,
                   updated_at
            FROM inventory
            WHERE quantity > 0
            ORDER BY category, item_name
        ");

        if (!$stmt->execute()) {
            throw new Exception($stmt->error);
        }

        $result = $stmt->get_result();
        $items = array();

        while ($row = $result->fetch_assoc()) {
            $items[] = array(
                'id' => (int)$row['id'],
                'item_name' => $row['item_name'],
                'quantity' => (int)$row['quantity'],
                'category' => $row['category'],
                'unit_of_measurement' => $row['unit_of_measurement'],
                'cost_per_unit' => $row['cost_per_unit'] ? (float)$row['cost_per_unit'] : null,
                'minimum_stock' => $row['minimum_stock'] ? (int)$row['minimum_stock'] : null,
                'notes' => $row['notes'],
                'last_restocked' => $row['last_restocked'],
                'updated_at' => $row['updated_at']
            );
        }

        echo json_encode($items);
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