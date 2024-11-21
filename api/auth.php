<?php
require_once 'dbcontroller.php';
require_once 'response.php';
require_once 'jwt.php';
require_once 'auth.php';

function handleRequest($method, $segments) {
    if ($method !== 'POST') {
        respondWithError("Method not allowed", 405);
        return;
    }

    $routeHandlers = [
        '' => 'login',
        'refresh' => 'refresh',
        'logout' => 'logout'
    ];

    $route = implode('/', $segments);
    if (isset($routeHandlers[$route])) {
        $handlerFunction = $routeHandlers[$route];
        $handlerFunction();
    } else {
        respondWithError("Route not found", 404);
    }
}

// Handle login: authenticate user and return JWT
function login() {
    $data = json_decode(file_get_contents("php://input"), true);
    $username = $data['username'] ?? '';
    $password = $data['password'] ?? '';

    if (empty($username) || empty($password)) {
        respondWithError("Username and password are required", 400);
    }

    $userData = executeQuery("SELECT user_id, password_hash, role FROM users WHERE username = :username",
        [':username' => $username]);

    if (password_verify($password, $userData['password_hash'])) {
        $jwt = generateJwt($userData['user_id'], $userData['role']);

        // Create and store refresh token
        $refreshToken = bin2hex(random_bytes(32));
        $expiresAt = date('Y-m-d H:i:s', time() + 86400); // 1-day expiration for refresh token
        storeRefreshToken($userData['user_id'], $refreshToken, $expiresAt);

        respondWithSuccess(200, "Login successful", ['jwt' => $jwt, 'refresh_token' => $refreshToken]);
    } else {
        respondWithError("Invalid credentials", 401);
    }
}

// Handle refresh: validate refresh token and return new JWT
function refresh() {
    $data = json_decode(file_get_contents("php://input"), true);
    $refreshToken = $data['refresh_token'] ?? '';

    if (empty($refreshToken)) {
        respondWithError("Refresh token is required", 400);
    }

    // Validate and refresh JWT using the provided refresh token
    $userData = refreshJwt($refreshToken);

    if (!$userData) {
        respondWithError("Invalid or expired refresh token", 401);
    }

    $jwt = generateJwt($userData->userId, $userData->role);

    respondWithSuccess(200, "JWT refreshed successfully", ['jwt' => $jwt]);
}

// Handle logout: mark the refresh token as expired
function logout() {
    $data = json_decode(file_get_contents("php://input"), true);
    $refreshToken = $data['refresh_token'] ?? '';

    if (empty($refreshToken)) {
        respondWithError("Refresh token is required", 400);
    }

    // Mark the refresh token as expired in the database
    markTokenAsExpired($refreshToken);

    respondWithSuccess(200, "Logged out successfully");
}

?>
