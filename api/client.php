<?php
require_once 'dbcontroller.php';
require_once 'response.php';
require_once 'jwt.php';
require_once 'auth.php';

function handleRequest($method, $segments) {
    $authHeader = $_SERVER['HTTP_AUTHORIZATION'] ?? null;
    $userData = validateAuthorization($authHeader);

    if (!$userData) {
        respondWithError("Authorization header missing or malformed", 401);
    }

    $userRole = $userData->role;
    $activeUserId = $userData->userId;

    if ($userRole !== 'caterer') {
        respondWithError("Unauthorized access", 403);
    }

    // Map routes to their handlers
    $routeHandlers = [
        'GET' => [
            '' => 'getClients',
            'upcoming-events' => 'getUpcomingEvents',
            'menu-items' => 'getMenuItems',
            'email-domain' => 'getClientsByEmailDomain',
            'events' => 'getClientEvents'
        ],
        'POST' => [
            'archive' => 'archiveClient',
            'notes' => 'updateClientNotes',
            '' => 'createClient'
        ],
        'PUT' => [
            '{client_id}' => 'updateClientDetails'
        ],
        'DELETE' => [
            '{client_id}' => 'deleteClient'
        ]
    ];

    if (isset($routeHandlers[$method])) {
        array_shift($segments);
        $route = implode('/', $segments);
        if (isset($routeHandlers[$method][$route])) {
            $handlerFunction = $routeHandlers[$method][$route];
            $handlerFunction($segments);
        } else {
            respondWithError("Route not found", 404);
        }
    } else {
        respondWithError("Method not allowed", 405);
    }
}

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
    respondWithSuccess("Clients retrieved successfully", 200,  executeSelect(
        "SELECT client_id, client_name, phone_number, email_address FROM Clients"
    ));
}

function getUpcomingEvents($segments) {
    respondWithSuccess("Clients with upcoming events retrieved successfully", 200, executeSelect(
        "SELECT c.client_id, c.client_name, e.event_id, e.event_description
        FROM Clients c JOIN Events e ON c.client_id = e.client_id
        WHERE e.start_date > CURRENT_DATE"
    ));
}

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

function getClientEvents($segments) {
    $client_id = $segments[1];
    if (!validateClientId($client_id)) {
        respondWithError("Invalid client ID", 400);
    }
    if (!checkClientExists($client_id)) {
        respondWithError("Client not found", 404);
    }
    respondWithSuccess("Events for client $client_id retrieved successfully", 200, executeSelect(
        "SELECT event_id, event_description, start_date, end_date, event_status
        FROM Events WHERE client_id = :client_id", [':client_id' => $client_id]
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
    if (executeChange(
        "UPDATE Clients SET notes = :notes WHERE client_id = :client_id",
        [':notes' => $notes, ':client_id' => $client_id]
    )) {
        respondWithSuccess("Client notes updated successfully", 200);
    } else {
        respondWithError("Error updating client notes", 500);
    }
}

function createClient($segments) {
    $data = json_decode(file_get_contents("php://input"), true);
    $db = new DBController();
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
    ])) {
        $newClientId = $db->conn->lastInsertId();
        respondWithSuccess("Client created successfully with ID: $newClientId", 201);
    } else {
        respondWithError("Error creating client", 500);
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
    if (executeChange(
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
        respondWithSuccess("Client updated successfully", 200);
    } else {
        respondWithError("Error updating client", 500);
    }
}

function deleteClient($segments) {
    $client_id = $segments[0];
    if (!validateClientId($client_id)) {
        respondWithError("Invalid client ID", 400);
    }
    if (!checkClientExists($client_id)) {
        respondWithError("Client not found", 404);
    }
    if (executeChange(
        "DELETE FROM Clients WHERE client_id = :client_id",
        [':client_id' => $client_id]
    )) {
        respondWithSuccess("Client deleted successfully", 200);
    } else {
        respondWithError("Error deleting client", 500);
    }
}
?>
