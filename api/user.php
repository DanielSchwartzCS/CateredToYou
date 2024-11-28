<?php
require_once 'dbcontroller.php';
require_once 'response.php';
require_once 'validators.php';

// Fetch all users
function fetchAllUsers($segments) {
    respondWithSuccess("All users retrieved", 200, executeSelect("SELECT * FROM users"));
}

// Fetch a specific user
function fetchUser($segments) {
    $user_id = validateResource($segments[0]);

    if (!$user_id && is_numeric($user_id)) {
        respondWithError("User ID is required and should be numeric", 400);
    }
    $response = executeSelect(
        "SELECT * FROM users WHERE user_id = :user_id",
        [':user_id' => $user_id], false
    );
    if ($response) {
        respondWithSuccess("User $user_id retrieved", 200, $response);
    } else {
        respondWithError("User not found", 204);
    }
}

// Change user password
function changePassword($segments) {
    $user_id = $segments[0] ?? null;
    $inputData = json_decode(file_get_contents('php://input'), true);
    $newPassword = $inputData['new_password'] ?? null;

    if (!$user_id || !is_numeric($user_id) || !$newPassword) {
        respondWithError("Numerical user ID and new password are required", 400);
    }

    $password_hash = password_hash($newPassword, PASSWORD_DEFAULT);
    if (!executeChange(
        "UPDATE users SET password_hash = :password_hash WHERE user_id = :user_id",
        [':password_hash' => $password_hash, ':user_id' => $user_id]
    )) {
        respondWithError("Failed to change password", 500);
    }
}

// Change user details (excluding role and password)
function changeUserDetails($segments) {
    $user_id = $segments[0] ?? null;
    $inputData = json_decode(file_get_contents('php://input'), true);

    if (!$user_id || !is_numeric($user_id)) {
        respondWithError("User ID is required and should be numeric", 400);
    }

    // Validate required fields (first_name, last_name, phone_number, email_address, employment_status)
    $fields = ['first_name', 'last_name', 'phone_number', 'email_address', 'employment_status'];
    foreach ($fields as $field) {
        if (!isset($inputData[$field]) || empty($inputData[$field])) {
            respondWithError("Field $field is required", 400);
        }
    }


    // Update user details
    if (!executeChange(
        "UPDATE users SET first_name = :first_name, last_name = :last_name,
         phone_number = :phone_number, email_address = :email_address,
         employment_status = :employment_status WHERE user_id = :user_id",
        [
            ':first_name' => $inputData['first_name'],
            ':last_name' => $inputData['last_name'],
            ':phone_number' => $inputData['phone_number'],
            ':email_address' => $inputData['email_address'],
            ':employment_status' => $inputData['employment_status'],
            ':user_id' => $user_id
        ]
    )) {
        respondWithError("Failed to update user details", 500);
    }

    respondWithSuccess("User details updated successfully", 200);
}

// Change user employment status
function changeEmploymentStatus($segments) {
    $user_id = $segments[1] ?? null;
    $inputData = json_decode(file_get_contents('php://input'), true);
    $employment_status = $inputData['employment_status'] ?? null;

    if (!$user_id || !is_numeric($user_id) || !$employment_status) {
        respondWithError("User ID and employment status are required", 400);
    }

    // Update employment status
    if (!executeChange(
        "UPDATE users SET employment_status = :employment_status WHERE user_id = :user_id",
        [':employment_status' => $employment_status, ':user_id' => $user_id]
    )) {
        respondWithError("Failed to update employment status", 500);
    }

    respondWithSuccess("Employment status updated successfully", 200);
}

// Change user role
function changeUserRole($segments) {
    $user_id = $segments[1] ?? null;
    $inputData = json_decode(file_get_contents('php://input'), true);
    $role = $inputData['role'] ?? null;

    if (!$user_id || !is_numeric($user_id) || !$role || !in_array($role, ['caterer', 'employee'])) {
        respondWithError("User ID and valid role ('caterer' or 'employee') are required", 400);
    }

    // Update role
    if (!executeChange(
        "UPDATE users SET role = :role WHERE user_id = :user_id",
        [':role' => $role, ':user_id' => $user_id]
    )) {
        respondWithError("Failed to update user role", 500);
    }

    respondWithSuccess("User role updated successfully", 200);
}
?>
