<?php
//api/resources/client.php
require_once 'dbcontroller.php';
require_once 'response.php';
require_once 'jwt.php';
require_once 'auth.php';
require_once 'validators.php';

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
    $client_id = validateResource($segments, 1, 'posInt');

    if (!checkClientExists($client_id)) {
        respondWithError("Client not found", 204);
    }

    $data = validateBody(['notes' => 'string']);
    $notes = $data['notes'] ?? '';

    if (!executeChange(
        "UPDATE Clients SET notes = :notes WHERE client_id = :client_id",
        [':notes' => $notes, ':client_id' => $client_id])
    ) {
        respondWithError("Failed to update client notes", 400);
    }
}

function createClient($segments) {
    $fieldsAndTypes = [
        'client_name' => 'string',
        'phone' => 'string',
        'email_address' => 'email',
        'billing_address' => 'string',
        'preferred_contact_method' => 'string',
    ];

    $data = validateBody($fieldsAndTypes);

    if (!executeInsert(
        "INSERT INTO Clients (client_name, phone, email_address, billing_address,
        preferred_contact_method, notes) VALUES (:client_name, :phone, :email_address,
        :billing_address, :preferred_contact_method, :notes)",
        [
            ':client_name' => $data['client_name'],
            ':phone' => $data['phone'],
            ':email_address' => $data['email_address'],
            ':billing_address' => $data['billing_address'],
            ':preferred_contact_method' => $data['preferred_contact_method'],
            ':notes' => $data['notes']
        ]
    )) {
        respondWithError("Client not created", 400);
    }
}

function updateClientDetails($segments) {
    if (!is_array($segments) || empty($segments[0])) {
        respondWithError("Client ID is required", 400);
    }

    $client_id = validate($segments[0], 'posInt');
    if (!validateClientId($client_id)) {
        respondWithError("Invalid client ID", 400);
    }
    if (!checkClientExists($client_id)) {
        respondWithError("Client not found", 204);
    }
    $data = json_decode(file_get_contents("php://input"), true);
    if (!validateClientData($data)) {
        respondWithError("Missing required client data", 400);
    }

    if (!executeChange(
        "UPDATE Clients SET client_name = :client_name, phone = :phone,
        email_address = :email_address, billing_address = :billing_address,
        preferred_contact_method = :preferred_contact_method, notes = :notes
        WHERE client_id = :client_id", [
        ':client_name' => $data['client_name'],
        ':phone' => $data['phone'],
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
        respondWithError("Client not found", 204);
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
