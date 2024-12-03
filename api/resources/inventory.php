<?php
// api/resources/inventory.php
require_once __DIR__ . '/../database/dbcontroller.php';
require_once __DIR__ . '/../utils/response.php';
require_once __DIR__ . '/../data-processors/input.php';

function fetchAllInventory() {
    $inventory = executeSelect("SELECT * FROM inventory");
    if (empty($inventory)) {
            respondSuccess(null, 204);
    }
    respondSuccess($inventory);
}

function fetchInventoryById($inventoryId) {
    $inventory = executeSelect(
        "SELECT * FROM inventory WHERE inventory_id = :inventory_id",
        [':inventory_id' => $inventoryId],
        false
    );

    if (!$inventory) {
        respondError("Inventory item not found", 404);
    }

    respondSuccess($inventory);
}

function fetchInventoryByEvent($eventId) {
    $inventory = executeSelect(
        "SELECT i.inventory_id, i.item_name, e.quantity, i.unit, i.display_unit, i.location_id
         FROM event_inventory_needs e
         JOIN inventory i ON e.inventory_id = i.inventory_id
         WHERE e.event_id = :event_id",
        [':event_id' => $eventId]
    );

    if (empty($inventory)) {
        respondError("No inventory found for the specified event", 204);
    }

    respondSuccess($inventory);
}

function createInventory() {
    $requiredFields = [
        'item_name' => 'string',
        'unit' => 'string',
        'display_unit' => 'string',
        'quantity_in_stock' => 'nonNegInt',
        'location_id' => 'posInt'
    ];

    $data = getValidHttpBody($requiredFields);

    $inventoryIds = [];
    foreach ($data as $item) {
        $inventoryId = executeInsert(
            "INSERT INTO inventory (
                item_name,
                unit,
                display_unit,
                quantity_in_stock,
                location_id
            ) VALUES (
                :item_name,
                :unit,
                :display_unit,
                :quantity_in_stock,
                :location_id
            )",
            $item
        );
        $inventoryIds[] = $inventoryId;
    }

    respondSuccess(['inventory_ids' => $inventoryIds], 201);
}

function updateInventoryEntry($inventoryId) {
    $updateFields = [
        'item_name' => 'string',
        'unit' => 'string',
        'display_unit' => 'string',
        'quantity_in_stock' => 'nonNegInt',
        'location_id' => 'posInt'
    ];

    $data = getValidHttpBody($updateFields)[0];

    $result = executeChange(
        "UPDATE inventory SET
        item_name = :item_name,
        unit = :unit,
        display_unit = :display_unit,
        quantity_in_stock = :quantity_in_stock,
        location_id = :location_id
        WHERE inventory_id = :inventory_id",
        array_merge($data, ['inventory_id' => $inventoryId])
    );

    respondSuccess(null);
}

function updateInventoryQuantity($inventoryId) {
    $updateFields = [
        'quantity_in_stock' => 'nonNegInt'
    ];

    $data = getValidHttpBody($updateFields)[0];

    $result = executeChange(
        "UPDATE inventory SET
        quantity_in_stock = :quantity_in_stock
        WHERE inventory_id = :inventory_id",
        array_merge($data, ['inventory_id' => $inventoryId])
    );

    respondSuccess(null);
}

?>