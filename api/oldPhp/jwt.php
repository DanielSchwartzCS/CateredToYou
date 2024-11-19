<?php
require_once 'response.php';

// Function to generate a JWT token
function generateJwt($userId, $role) {
    $secretKey = "secret_key"; // TODO: make this an env var
    $issuedAt = time();
    $expirationTime = $issuedAt + 3600; // JWT expiration time

    $payload = [
        'iat' => $issuedAt,
        'exp' => $expirationTime,
        'userId' => $userId,
        'role' => $role
    ];

    return JWT::encode($payload, $secretKey);
}

// Function to validate a JWT token
function validateJwt($jwt) {
    $secretKey = "secret_key"; // TODO: make this an env var
    try {
        $decoded = JWT::decode($jwt, $secretKey, ['HS256']);
        return $decoded;
    } catch (Exception $e) {
        respondWithError("Invalid token: " . $e->getMessage(), 401);
    }
}
?>
