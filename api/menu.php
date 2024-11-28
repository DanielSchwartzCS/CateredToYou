<?php
require_once 'dbcontroller.php';
require_once 'response.php';

// Fetch all menu items
function fetchMenuItems() {
    try {
        // Fetch all menu items from the database
        $menuItems = executeSelect("SELECT * FROM menu_items");
        respondWithSuccess("Menu items retrieved", 200, $menuItems);
    } catch (Exception $e) {
        respondWithError("Failed to fetch menu items: " . $e->getMessage(), 500);
    }
}

function fetchEventMenuItems($segments) {
    $eventId = $segments[1] ?? null;

    if (empty($eventId)) {
        respondWithError("Event ID is required", 400);
    }

    try {
        // Fetch menu items associated with a specific event
        $menuItems = executeSelect(
            "SELECT mi.* FROM menu_items mi
            JOIN event_menu em ON mi.item_id = em.item_id
            WHERE em.event_id = :event_id",
            [':event_id' => $eventId]
        );
        respondWithSuccess(200, "Menu items for event $eventId retrieved", $menuItems);
    } catch (Exception $e) {
        respondWithError("Failed to fetch menu items for event: " . $e->getMessage(), 500);
    }
}

function createMenuItem() {
    $data = json_decode(file_get_contents("php://input"), true);
    $name = $data['name'] ?? '';
    $description = $data['description'] ?? '';
    $price = $data['price'] ?? 0;

    if (empty($name) || empty($description)) {
        respondWithError("Menu item name and description are required", 400);
    }

    try {
        // Insert new menu item into the database
        $result = executeChange(
            "INSERT INTO menu_items (name, description, price) VALUES (:name, :description, :price)",
            [':name' => $name, ':description' => $description, ':price' => $price]
        );

        if ($result) {
            respondWithSuccess(201, "Menu item created successfully");
        } else {
            respondWithError("Failed to create menu item", 500);
        }
    } catch (Exception $e) {
        respondWithError("Failed to create menu item: " . $e->getMessage(), 500);
    }
}

function updateMenuItem($segments) {
    $itemId = $segments[1] ?? null;
    $data = json_decode(file_get_contents("php://input"), true);

    if (empty($itemId) || empty($data)) {
        respondWithError("Menu item ID and data are required", 400);
    }

    $name = $data['name'] ?? null;
    $description = $data['description'] ?? null;
    $price = isset($data['price']) ? $data['price'] : null;

    $updateFields = [];
    $params = [':item_id' => $itemId];

    if ($name !== null) {
        $updateFields[] = "name = :name";
        $params[':name'] = $name;
    }

    if ($description !== null) {
        $updateFields[] = "description = :description";
        $params[':description'] = $description;
    }

    if ($price !== null) {
        $updateFields[] = "price = :price";
        $params[':price'] = $price;
    }

    if (empty($updateFields)) {
        respondWithError("No data to update", 400);
    }

    try {
        // Update menu item in the database
        $query = "UPDATE menu_items SET " . implode(", ", $updateFields) . " WHERE item_id = :item_id";
        $result = executeChange($query, $params);

        if ($result) {
            respondWithSuccess(200, "Menu item updated successfully");
        } else {
            respondWithError("Menu item not found or no changes made", 404);
        }
    } catch (Exception $e) {
        respondWithError("Failed to update menu item: " . $e->getMessage(), 500);
    }
}

function deleteMenuItem($segments) {
    $itemId = $segments[1] ?? null;

    if (empty($itemId)) {
        respondWithError("Menu item ID is required", 400);
    }

    try {
        // Delete menu item from the database
        $result = executeChange(
            "DELETE FROM menu_items WHERE item_id = :item_id",
            [':item_id' => $itemId]
        );

        if ($result) {
            respondWithSuccess(200, "Menu item deleted successfully");
        } else {
            respondWithError("Menu item not found", 204);
        }
    } catch (Exception $e) {
        respondWithError("Failed to delete menu item: " . $e->getMessage(), 500);
    }
}
?>
