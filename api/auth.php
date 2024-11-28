<?php
require_once 'dbcontroller.php';
require_once 'response.php';
require_once 'jwt.php';
require_once 'token.php';

// Handle login: authenticate user and return JWT
function login($segments) {
    $data = json_decode(file_get_contents("php://input"), true);
    $username = $data['username'] ?? '';
    $password = $data['password'] ?? '';

    if (empty($username) || empty($password)) {
        respondWithError("Username and password are required", 400);
    }

    $userData = executeSelect("SELECT user_id, password_hash, role FROM users WHERE username = :username",
            [':username' => $username], false);
    if ($userData && password_verify($password, $userData['password_hash'])) {
        $jwt = generateJwt($userData['user_id'], $userData['role']);

        // Create and store refresh token
        $refreshToken = bin2hex(random_bytes(32));
        $expiresAt = date('Y-m-d H:i:s', time() + 86400); // 1-day expiration for refresh token
        storeRefreshToken($userData['user_id'], $refreshToken, $expiresAt);

        respondWithSuccess("Login successful", 200, ['jwt' => $jwt, 'refresh_token' => $refreshToken]);
    } else {
        respondWithError("Invalid credentials", 401);
    }
}

// Handle refresh: validate refresh token and return new JWT
function refresh($segments) {
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

    respondWithSuccess("JWT refreshed successfully", 200, ['jwt' => $jwt]);
}

// Handle logout: mark the refresh token as expired
function logout($segments) {
    $data = json_decode(file_get_contents("php://input"), true);
    $refreshToken = $data['refresh_token'] ?? '';

    if (empty($refreshToken)) {
        respondWithError("Refresh token is required", 400);
    }

    // Mark the refresh token as expired in the database
    markTokenAsExpired($refreshToken);

    respondWithSuccess("Logged out successfully", 200);
}

?>