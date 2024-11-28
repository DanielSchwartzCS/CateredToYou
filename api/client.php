<?php
require_once 'dbcontroller.php';
require_once 'response.php';
require_once 'jwt.php';
require_once 'auth.php';

function validateClientId($client_id) {
    return is_numeric($client_id) && $client_id > 0;
}

function checkClientExists($client_id) {
    $result = executeSelect(
        "SELECT client_id FROM Clients WHERE client_id = :client_id",
        [':client_id' => $client_id],
        false
    );
    return !empty($result);
}

function getClients($segments) {
    respondWithSuccess("Clients retrieved successfully", 200,
        executeSelect("SELECT * FROM Clients"));
}

function getUpcomingEvents($segments) {
    respondWithSuccess("Clients with upcoming events retrieved successfully", 200,
        executeSelect(
        "SELECT c.client_id, c.client_name, e.event_id, e.event_description
        FROM Clients c JOIN Events e ON c.client_id = e.client_id
        WHERE e.start_date > CURRENT_DATE"
    ));
}

function updateClientNotes($segments) {
    $client_id = $segments[1];
    if (!validateClientId($client_id)) {
        respondWithError("Invalid client ID", 400);
    }
    if (!checkClientExists($client_id)) {
        respondWithError("Client not found", 404);
    }
    $data = json_decode(file_get_contents("php://input"), true);
    $notes = $data['notes'] ?? '';
    if (!executeChange(
        "UPDATE Clients SET notes = :notes WHERE client_id = :client_id",
        [':notes' => $notes, ':client_id' => $client_id])) {
        respondWithError("Failed to update client notes", 400);
        }
}

function createClient($segments) {
    $data = json_decode(file_get_contents("php://input"), true);
    if (executeChange(
        "INSERT INTO Clients (client_name, phone_number, email_address, billing_address,
        preferred_contact_method, notes) VALUES (:client_name, :phone_number, :email_address,
        :billing_address, :preferred_contact_method, :notes)", [
        ':client_name' => $data['client_name'],
        ':phone_number' => $data['phone_number'],
        ':email_address' => $data['email_address'],
        ':billing_address' => $data['billing_address'],
        ':preferred_contact_method' => $data['preferred_contact_method'],
        ':notes' => $data['notes']
    ]) {
        $newClientId = $db->conn->lastInsertId();
        respondWithSuccess("Client created successfully with ID: $newClientId", 201);
    } else {
        respondWithError("Client was not created", 400);
    }
}

function updateClientDetails($segments) {
    $client_id = $segments[0];
    if (!validateClientId($client_id)) {
        respondWithError("Invalid client ID", 400);
    }
    if (!checkClientExists($client_id)) {
        respondWithError("Client not found", 404);
    }
    $data = json_decode(file_get_contents("php://input"), true);
    if (!validateClientData($data)) {
        respondWithError("Missing required client data", 400);
    }
    if (!executeChange(
        "UPDATE Clients SET client_name = :client_name, phone_number = :phone_number,
        email_address = :email_address, billing_address = :billing_address,
        preferred_contact_method = :preferred_contact_method, notes = :notes
        WHERE client_id = :client_id", [
        ':client_name' => $data['client_name'],
        ':phone_number' => $data['phone_number'],
        ':email_address' => $data['email_address'],
        ':billing_address' => $data['billing_address'],
        ':preferred_contact_method' => $data['preferred_contact_method'],
        ':notes' => $data['notes'],
        ':client_id' => $client_id
    ])) {
        respondWithError("Client not updated", 500);
    }
}
/*
function getMenuItems($segments) {
    $item_name = $segments[1] ?? '';
    if (empty($item_name)) {
        respondWithError("Menu item name is required", 400);
    }
    respondWithSuccess("Clients who requested $item_name retrieved successfully", 200, executeSelect(
        "SELECT c.client_id, c.client_name FROM Clients c JOIN Events e ON c.client_id = e.client_id
        JOIN EventMenu em ON e.event_id = em.event_id JOIN MenuItems m ON em.menu_item_id = m.menu_item_id
        WHERE m.item_name = :item_name", [':item_name' => $item_name]
    ));
}

function getClientsByEmailDomain($segments) {
    $domain = $segments[1] ?? '';
    if (empty($domain)) {
        respondWithError("Email domain is required", 400);
    }
    respondWithSuccess("Clients with email domain $domain retrieved successfully", 200, executeSelect(
        "SELECT client_id, client_name, email_address FROM Clients WHERE email_address LIKE :domain",
        [':domain' => "%@$domain"]
    ));
}

function archiveClient($segments) {
    $client_id = $segments[1];
    if (!validateClientId($client_id)) {
        respondWithError("Invalid client ID", 400);
    }
    if (!checkClientExists($client_id)) {
        respondWithError("Client not found", 404);
    }
    if (executeChange(
        "UPDATE Clients SET archived = TRUE WHERE client_id = :client_id",
        [':client_id' => $client_id]
    )) {
        respondWithSuccess("Client archived successfully", 200);
    } else {
        respondWithError("Error archiving client", 500);
    }
}
*/
?>
