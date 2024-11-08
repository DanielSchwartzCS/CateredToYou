<?php
require_once 'dbcontroller.php';
require_once 'response.php';
require_once 'jwt.php';
require_once 'auth.php';

function executeQuery($query, $params = [], $fetchMethod = PDO::FETCH_ASSOC) {
    $db = new DBController();
    $stmt = $db->conn->prepare($query);
    $stmt->execute($params);

    return $fetchMethod == PDO::FETCH_ASSOC
        ? $stmt->fetchAll(PDO::FETCH_ASSOC)
        : $stmt->fetch(PDO::FETCH_ASSOC);
}

function handleRequest($method, $segments) {
    $authHeader = $_SERVER['HTTP_AUTHORIZATION'] ?? null;
    $userData = validateAuthorization($authHeader);

    if (!$userData) {
        respondWithError("Authorization header missing or malformed", 401);
    }

    $userRole = $userData->role;
    $activeUserId = $userData->userId;

    // Only caterer has full access; individual users can access their own data
    if ($userRole !== 'caterer' && $activeUserId !== $segments[1]) {
        respondWithError("Unauthorized access", 403);
    }

    $routeHandlers = [
        'GET' => [
            '' => 'getAllUsers', // Get all users for caterers only
            '{user_id}' => 'getUserById' // Get specific user details
        ],
        'POST' => [
            '' => 'createUser' // Only caterers can create users
        ],
        'PUT' => [
            '{user_id}' => 'updateUserDetails' // Update user details
        ],
        'DELETE' => [
            '{user_id}' => 'deleteUser' // Delete a user
        ]
    ];

    array_shift($segments);
    $route = implode('/', $segments);
    if (isset($routeHandlers[$method][$route])) {
        $handlerFunction = $routeHandlers[$method][$route];
        $handlerFunction($segments);
    } else {
        respondWithError("Route not found", 404);
    }
}

function validateUserId($user_id) {
    return is_numeric($user_id) && $user_id > 0;
}

function checkUserExists($user_id) {
    $result = executeQuery(
        "SELECT id FROM users WHERE id = :user_id",
        [':user_id' => $user_id]
    );
    return !empty($result);
}

// GET /user endpoint for retrieving all users
function getAllUsers($segments) {
    respondWithSuccess(200, "Users retrieved successfully", executeQuery(
        "SELECT id, username, role, first_name, last_name, email, phone FROM users"
    ));
}

// GET /user/{user_id} endpoint for retrieving specific user details
function getUserById($segments) {
    $user_id = $segments[1];
    if (!validateUserId($user_id)) {
        respondWithError("Invalid user ID", 400);
    }
    if (!checkUserExists($user_id)) {
        respondWithError("User not found", 404);
    }
    respondWithSuccess(200, "User retrieved successfully", executeQuery(
        "SELECT id, username, role, first_name, last_name, email, phone FROM users WHERE id = :user_id",
        [':user_id' => $user_id]
    ));
}

// POST /user endpoint for creating a new user (Caterer only)
function createUser($segments) {
    $data = json_decode(file_get_contents("php://input"), true);
    $passwordHash = password_hash($data['password'], PASSWORD_DEFAULT);

    if (executeQuery(
        "INSERT INTO users (username, password, role, first_name, last_name, email, phone)
        VALUES (:username, :password, 'employee', :first_name, :last_name, :email, :phone)", [
        ':username' => $data['username'],
        ':password' => $passwordHash,
        ':first_name' => $data['first_name'],
        ':last_name' => $data['last_name'],
        ':email' => $data['email'],
        ':phone' => $data['phone']
    ])) {
        respondWithSuccess(201, "User created successfully");
    } else {
        respondWithError("Error creating user", 500);
    }
}

// PUT /user/{user_id} endpoint for updating user details
function updateUserDetails($segments) {
    $user_id = $segments[1];
    if (!validateUserId($user_id)) {
        respondWithError("Invalid user ID", 400);
    }
    if (!checkUserExists($user_id)) {
        respondWithError("User not found", 404);
    }

    $data = json_decode(file_get_contents("php://input"), true);

    if (executeQuery(
        "UPDATE users SET username = :username, first_name = :first_name, last_name = :last_name,
        email = :email, phone = :phone WHERE id = :user_id", [
        ':username' => $data['username'],
        ':first_name' => $data['first_name'],
        ':last_name' => $data['last_name'],
        ':email' => $data['email'],
        ':phone' => $data['phone'],
        ':user_id' => $user_id
    ])) {
        respondWithSuccess(200, "User updated successfully");
    } else {
        respondWithError("Error updating user", 500);
    }
}

// DELETE /user/{user_id} endpoint for deleting a user (Caterer only)
function deleteUser($segments) {
    $user_id = $segments[1];
    if (!validateUserId($user_id)) {
        respondWithError("Invalid user ID", 400);
    }
    if (!checkUserExists($user_id)) {
        respondWithError("User not found", 404);
    }

    if (executeQuery(
        "DELETE FROM users WHERE id = :user_id",
        [':user_id' => $user_id]
    )) {
        respondWithSuccess(200, "User deleted successfully");
    } else {
        respondWithError("Error deleting user", 500);
    }
}
?>
