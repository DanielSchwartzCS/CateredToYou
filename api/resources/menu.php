<?php
// api/resources/menu.php
require_once __DIR__ . '/../database/dbcontroller.php';
require_once __DIR__ . '/../utils/response.php';
require_once __DIR__ . '/../data-processors/input.php';

// Fetch all menu items
function fetchMenuItems() {
    $menuItems = executeSelect("SELECT * FROM menu_items");

    if (empty($menuItems)) {
        respondSuccess(null, 204);
    }

    respondSuccess($menuItems);
}

// Fetch menu items for a specific event
//TODO: sql logic is completely wrong here, this is just a placeholder
function fetchEventMenuItems($eventId) {
    $menuItems = executeSelect(
        "SELECT mi.menu_item_id, mi.item_name, mi.item_type, mi.item_fancy_description, mi.item_price
         FROM event_menu em
         JOIN menu_items mi ON em.menu_item_id = mi.menu_item_id
         WHERE em.event_id = :event_id",
        [':event_id' => $eventId]
    );

    if (empty($menuItems)) {
        respondError("No menu items found for the specified event", 204);
    }

    respondSuccess($menuItems);
}

// Create a new menu item
function createMenuItem() {
    $requiredFields = [
        'item_name' => 'string',
        'item_type' => 'string',
        'item_fancy_description' => 'string',
        'item_price' => 'decimal'
    ];

    $data = getValidHttpBody($requiredFields)[0];

    $menuItemId = executeInsert(
        "INSERT INTO menu_items (item_name, item_type, item_fancy_description, item_price)
         VALUES (:item_name, :item_type, :item_fancy_description, :item_price)",
        $data
    );

    respondSuccess(['menu_item_id' => $menuItemId], 201);
}

// Update an existing menu item
function updateMenuItem() {
    $requiredFields = [
        'item_name' => 'string',
        'item_type' => 'string',
        'item_fancy_description' => 'string',
        'item_price' => 'decimal'
    ];

    $data = getValidHttpBody($requiredFields)[0];

    $result = executeChange(
        "UPDATE menu_items SET
        item_name = :item_name,
        item_type = :item_type,
        item_fancy_description = :item_fancy_description,
        item_price = :item_price
        WHERE menu_item_id = :menu_item_id",
        array_merge($data, ['menu_item_id' => $data['menu_item_id']])
    );

    respondSuccess(null);
}

?>
