<?php
require_once 'dbcontroller.php';
require_once 'response.php';

// Fetch all inventory items
function fetchAllInventory($segments) {
    respondWithSuccess("All inventory items retrieved", 200,
    executeSelect("SELECT * FROM inventory"));
}

// Fetch inventory related to a specific event
function fetchInventoryByEvent($segments) {
    $event_id = $segments[1] ?? null;

    if ($event_id && is_numeric($event_id)) {
        $response = executeSelect(
            "SELECT i.inventory_id, i.item_name, e.quantity, i.unit, i.display_unit, i.location_id
             FROM event_inventory_needs e
             JOIN inventory i ON e.inventory_id = i.inventory_id
             WHERE e.event_id = :event_id",
            [':event_id' => $event_id]
        );
        respondWithSuccess("Inventory for event $event_id retrieved", 200, $response);
    } else {
        respondWithError("Event ID is required and should be numeric", 400);
    }
}


// Create inventory item(s) (handles both single and multiple items)
function createInventory($segments) {
    $inputData = json_decode(file_get_contents('php://input'), true);

    // Check if the input is an array of items or a single item
    if (empty($inputData)) {
        respondWithError("No data provided", 400);
    }

    // If it's a single item, convert it into an array for uniform processing
    if (!is_array($inputData)) {
        $inputData = [$inputData];
    }

    $values = [];
    foreach ($inputData as $index => $item) {
        // Validate required fields for each item
        if (empty($item['item_name']) || empty($item['unit']) ||
            empty($item['display_unit']) || empty($item['quantity_in_stock']) ||
            empty($item['location_id'])) {
            respondWithError("Required fields are missing for one or more inventory items", 400);
        }

        // Prepare the values for the batch insert
        $values[] = "(:item_name" . $index . ", :unit" . $index . ", :display_unit" . $index . ", :quantity_in_stock" . $index . ", :location_id" . $index . ")";
    }

    // Build the SQL query dynamically based on whether it's a single or multiple items
    $query = "INSERT INTO inventory (item_name, unit, display_unit, quantity_in_stock, location_id)
              VALUES " . implode(", ", $values);

    // Prepare the parameters for the batch insert
    $params = [];
    foreach ($inputData as $index => $item) {
        $params[":item_name" . $index] = $item['item_name'];
        $params[":unit" . $index] = $item['unit'];
        $params[":display_unit" . $index] = $item['display_unit'];
        $params[":quantity_in_stock" . $index] = $item['quantity_in_stock'];
        $params[":location_id" . $index] = $item['location_id'];
    }

    // Execute the query
    if (!executeChange($query, $params)) {
        //TODO: maybe in frontend make sure we don't lose all the attempted updates on failure
        respondWithError("Failed to create inventory item(s)", 500);
    }
}

// Update inventory quantity
function updateInventoryQuantity($segments) {
    $inventory_id = $segments[0];
    $inputData = json_decode(file_get_contents('php://input'), true);
    $quantity = $inputData['quantity'] ?? null;

    if ($quantity === null) {
        respondWithError("Quantity is required", 400);
    }

    // Get current quantity from database
    $currentQuantity = executeSelect("SELECT quantity FROM inventory WHERE inventory_id = :inventory_id",
        [':inventory_id' => $inventory_id], false);

    if (!$currentQuantity) {
        respondWithError("Inventory item $inventory_id not found", 204);
    }

    // Update the inventory quantity
    if (!executeChange(
        "UPDATE inventory SET quantity = :quantity WHERE inventory_id = :inventory_id",
        [':quantity' => $quantity, ':inventory_id' => $inventory_id]
    )
    && $quantity !== $currentQuantity['quantity']) {
        respondWithError("Failed to update inventory quantity", 500);
    }
}


// Update entire inventory entry
function updateInventoryEntry($segments) {
    $inventory_id = $segments[0];
    $inputData = json_decode(file_get_contents('php://input'), true);

    if (empty($inputData['item_name']) || empty($inputData['unit']) ||
        empty($inputData['display_unit']) || empty($inputData['quantity']) ||
        empty($inputData['location_id'])) {
        respondWithError("Required fields are missing", 400);
    }

    if (!executeChange(
        "UPDATE inventory SET item_name = :item_name, unit = :unit, display_unit = :display_unit,
         quantity = :quantity, location_id = :location_id
         WHERE inventory_id = :inventory_id",
        [
            ':item_name' => $inputData['item_name'],
            ':unit' => $inputData['unit'],
            ':display_unit' => $inputData['display_unit'],
            ':quantity' => $inputData['quantity'],
            ':location_id' => $inputData['location_id'],
            ':inventory_id' => $inventory_id
        ]
    )) {
        respondWithError("Failed to update inventory entry", 500);
    }
}
?>
