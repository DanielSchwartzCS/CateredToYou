<?php
require_once 'dbcontroller.php';
require_once 'response.php';
require_once 'jwt.php';

function handleRequest($method, $segments) {
    $db = new DBController();
    $authHeader = $_SERVER['HTTP_AUTHORIZATION'] ?? null;

    // Authorization check and user role extraction
    $userData = validateAuthorization($authHeader);

    if (!$userData) {
        respondWithError("Authorization header missing or malformed", 401);
    }
    $userRole = $userData->role;
    $activeUserId = $userData->userId;

    switch ($method) {
        case 'GET':
            if (empty($segments)) {
                // Get all users
                if ($userRole === 'caterer') {
                    $stmt = $db->conn->prepare("SELECT id, username, role, first_name, last_name, email, phone FROM users");
                    $stmt->execute();
                    $result = $stmt->fetchAll(PDO::FETCH_ASSOC);
                    respondWithSuccess(200, "Users retrieved successfully", $result);
                } else {
                    respondWithError("Unauthorized access", 403);
                }
            } else {
                // Fetch a specific user
                $userId = array_shift($segments);
                if ($userRole === 'caterer' || $userId == $activeUserId) {
                    $stmt = $db->conn->prepare("SELECT id, username, first_name, last_name, email, phone FROM users WHERE id = :id");
                    $stmt->execute([':id' => $userId]);
                    $result = $stmt->fetch(PDO::FETCH_ASSOC);
                    if ($result) {
                        respondWithSuccess(200, "User retrieved successfully", $result);
                    } else {
                        respondWithError("User not found", 404);
                    }
                } else {
                    respondWithError("Unauthorized access", 403);
                }
            }
            break;

        case 'POST':
            // Create a new user
            if ($userRole === 'caterer') {
                parse_str($_SERVER['QUERY_STRING'], $data);
                $stmt = $db->conn->prepare("INSERT INTO users (username, password, role, first_name, last_name, email, phone) VALUES (:username, :password, 'employee', :first_name, :last_name, :email, :phone)");
                $passwordHash = password_hash($data['password'], PASSWORD_DEFAULT);
                if ($stmt->execute([
                    ':username' => $data['username'],
                    ':password' => $passwordHash,
                    ':first_name' => $data['first_name'],
                    ':last_name' => $data['last_name'],
                    ':email' => $data['email'],
                    ':phone' => $data['phone']
                ])) {
                    $newUserId = $db->conn->lastInsertId();
                    respondWithSuccess(201, "User created successfully with ID: $newUserId");
                } else {
                    respondWithError("Error creating user", 500);
                }
            } else {
                respondWithError("Unauthorized access", 403);
            }
            break;

        case 'PUT':
            // Update user details
            $userId = array_shift($segments);
            if ($userRole === 'caterer' || $userId == $activeUserId) {
                parse_str($_SERVER['QUERY_STRING'], $data);
                $stmt = $db->conn->prepare("UPDATE users SET username = :username, first_name = :first_name, last_name = :last_name, email = :email, phone = :phone WHERE id = :id");
                if ($stmt->execute([
                    ':username' => $data['username'],
                    ':first_name' => $data['first_name'],
                    ':last_name' => $data['last_name'],
                    ':email' => $data['email'],
                    ':phone' => $data['phone'],
                    ':id' => $userId
                ])) {
                    if ($stmt->rowCount()) {
                        respondWithSuccess(200, "User updated successfully");
                    } else {
                        respondWithError("No changes made to user", 400);
                    }
                } else {
                    respondWithError("Error updating user", 500);
                }
            } else {
                respondWithError("Unauthorized access", 403);
            }
            break;

        case 'DELETE':
            // Delete user from the db
            if ($userRole === 'caterer') {
                $userId = array_shift($segments);
                $stmt = $db->conn->prepare("DELETE FROM users WHERE id = :id");
                if ($stmt->execute([':id' => $userId])) {
                    if ($stmt->rowCount()) {
                        respondWithSuccess(200, "User deleted successfully");
                    } else {
                        respondWithError("User not found", 404);
                    }
                } else {
                    respondWithError("Error deleting user", 500);
                }
            } else {
                respondWithError("Unauthorized access", 403);
            }
            break;

        default:
            respondWithError("Method not allowed", 405);
            break;
    }
}

/**
 * Returns the user if token is validated, null if authorization fails
 */
function validateAuthorization($authHeader) {
    if ($authHeader && preg_match('/Bearer\s(\S+)/', $authHeader, $matches)) {
        $jwt = $matches[1];
        return validateJwt($jwt); // Validate the token and get user data
    }
    return null;
}

// Entry point to handle the request
$method = $_SERVER['REQUEST_METHOD'];
$segments = explode('/', trim($_SERVER['PATH_INFO'], '/'));
handleRequest($method, $segments);
?>