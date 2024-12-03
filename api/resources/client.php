<?php
// api/resources/client.php
require_once __DIR__ . '/../database/dbcontroller.php';
require_once __DIR__ . '/../utils/response.php';
require_once __DIR__ . '/../data-processors/input.php';

// Fetch all clients
function fetchAllClients() {
    $clients = executeSelect("SELECT * FROM clients");

    if (empty($clients)) {
        respondSuccess(null, 204);
    }

    respondSuccess($clients);
}

// Fetch client by ID
function fetchClientById($clientId) {
    $client = executeSelect(
        "SELECT * FROM clients WHERE client_id = :client_id",
        [':client_id' => $clientId],
        false
    );

    if (!$client) {
        respondError("Client not found", 404);
    }

    respondSuccess($client);
}

// Create a new client
function createClient() {
    $requiredFields = [
        'client_name' => 'string',
        'phone' => 'string',
        'email' => 'email',
        'billing_address' => 'string',
        'preferred_contact_method' => 'string',
        'notes' => 'string'
    ];

    $data = getValidHttpBody($requiredFields)[0];

    // Check for existing client by email
    $existingClient = executeSelect(
        "SELECT client_id FROM clients WHERE email = :email",
        [':email' => $data['email']],
        false
    );

    if ($existingClient) {
        respondError("Client with this email already exists", 400);
    }

    $clientId = executeInsert(
        "INSERT INTO clients (
            client_name,
            phone,
            email,
            billing_address,
            preferred_contact_method,
            notes
        ) VALUES (
            :client_name,
            :phone,
            :email,
            :billing_address,
            :preferred_contact_method,
            :notes
        )",
        $data
    );

    respondSuccess(['client_id' => $clientId], 201);
}

// Update client details
function updateClientDetails($clientId) {
    $updateFields = [
        'client_name' => 'string',
        'phone' => 'string',
        'email' => 'email',
        'billing_address' => 'string',
        'preferred_contact_method' => 'string',
        'notes' => 'string'
    ];

    $data = getValidHttpBody($updateFields)[0];

    $result = executeChange(
        "UPDATE clients SET
        client_name = :client_name,
        phone = :phone,
        email = :email,
        billing_address = :billing_address,
        preferred_contact_method = :preferred_contact_method,
        notes = :notes
        WHERE client_id = :client_id",
        array_merge($data, ['client_id' => $clientId])
    );

    respondSuccess(null);
}

// Update client notes (Partial update)
function updateClientNotes($clientId) {
    $requiredFields = [
        'notes' => 'string'
    ];

    $data = getValidHttpBody($requiredFields)[0];

    $result = executeChange(
        "UPDATE clients SET notes = :notes WHERE client_id = :client_id",
        array_merge($data, ['client_id' => $clientId])
    );

    respondSuccess(null);
}

// Partial update of client notes
function patchClientNotes($clientId) {
    $requiredFields = [
        'notes' => 'string'
    ];

    $data = getValidHttpBody($requiredFields)[0];

    $result = executeChange(
        "UPDATE clients SET notes = :notes WHERE client_id = :client_id",
        array_merge($data, ['client_id' => $clientId])
    );

    respondSuccess(null);
}

?>
