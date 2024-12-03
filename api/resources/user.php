<?php
// api/resources/user.php
require_once __DIR__ . '/../database/dbcontroller.php';
require_once __DIR__ . '/../utils/response.php';
require_once __DIR__ . '/../data-processors/input.php';

// Helper function to check if user exists by ID
function checkUserExists($userId) {
    $existingUser = executeSelect(
        "SELECT user_id FROM users WHERE user_id = :user_id",
        [':user_id' => $userId],
        false
    );

    return $existingUser ? true : false;
}

// Fetch all users
function fetchAllUsers() {
    $users = executeSelect("SELECT * FROM users");

    if (empty($users)) {
        respondSuccess(null, 204);
    }

    respondSuccess($users);
}

// Fetch user by ID
function fetchUserById($userId) {
    if (!checkUserExists($userId)) {
        respondError("User not found", 404);
    }

    $user = executeSelect(
        "SELECT * FROM users WHERE user_id = :user_id",
        [':user_id' => $userId],
        false
    );

    respondSuccess($user);
}

// Create a new user
function createUser() {
    $requiredFields = [
        'first_name' => 'string',
        'last_name' => 'string',
        'phone' => 'string',
        'email' => 'email',
        'employment_status' => 'string',
        'role' => 'string',
        'password' => 'string'
    ];

    $data = getValidHttpBody($requiredFields)[0];

    if (!in_array($data['employment_status'], ['Active', 'Probation', 'On Leave', 'Exited'])) {
        respondError("Invalid employment status", 400);
    }

    $password_hash = password_hash($data['password'], PASSWORD_DEFAULT);

    $userId = executeInsert(
        "INSERT INTO users (first_name, last_name, phone, email, employment_status, role, password_hash)
        VALUES (:first_name, :last_name, :phone, :email, :employment_status, :role, :password_hash)",
        array_merge($data, ['password_hash' => $password_hash])
    );

    respondSuccess(['user_id' => $userId], 201);
}

// Update user details
function updateUserDetails($userId) {
    if (!checkUserExists($userId)) {
        respondError("User not found", 404);
    }

    $updateFields = [
        'first_name' => 'string',
        'last_name' => 'string',
        'phone' => 'string',
        'email' => 'email',
        'employment_status' => 'string'
    ];

    $data = getValidHttpBody($updateFields)[0];

    $result = executeChange(
        "UPDATE users SET first_name = :first_name, last_name = :last_name,
        phone = :phone, email = :email, employment_status = :employment_status
        WHERE user_id = :user_id",
        array_merge($data, ['user_id' => $userId])
    );

    respondSuccess(null);
}

// Update user role
function updateUserRole($userId) {
    if (!checkUserExists($userId)) {
        respondError("User not found", 404);
    }

    $requiredFields = ['role' => 'string'];

    $data = getValidHttpBody($requiredFields)[0];

    if (!in_array($data['role'], ['caterer', 'employee'])) {
        respondError("Invalid role", 400);
    }

    $result = executeChange(
        "UPDATE users SET role = :role WHERE user_id = :user_id",
        array_merge($data, ['user_id' => $userId])
    );

    respondSuccess(null);
}

// Change user password
function updatePassword($userId) {
    if (!checkUserExists($userId)) {
        respondError("User not found", 404);
    }

    $requiredFields = ['new_password' => 'string'];

    $data = getValidHttpBody($requiredFields)[0];

    $password_hash = password_hash($data['new_password'], PASSWORD_DEFAULT);

    $result = executeChange(
        "UPDATE users SET password_hash = :password_hash WHERE user_id = :user_id",
        [':password_hash' => $password_hash, ':user_id' => $userId]
    );

    if ($result) {
        respondSuccess("Password changed");
    } else {
        respondError("Password not changed", 400);
    }
}

// Change user employment status
function updateEmploymentStatus($userId) {
    if (!checkUserExists($userId)) {
        respondError("User not found", 404);
    }

    $requiredFields = ['employment_status' => 'string'];

    $data = getValidHttpBody($requiredFields)[0];

    $result = executeChange(
        "UPDATE users SET employment_status = :employment_status WHERE user_id = :user_id",
        array_merge($data, ['user_id' => $userId])
    );

    respondSuccess(null);
}
?>