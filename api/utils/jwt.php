<?php
// api/utils/jwt.php
require_once __DIR__ . '/response.php';
require_once __DIR__ . '/../vendor/autoload.php';
require_once __DIR__ . '/token.php';

use Firebase\JWT\JWT;
use Firebase\JWT\Key;

function generateJwt($userId, $role) {
    // TODO: Move these to environment configuration
    $secretKey = "secret_key"; // TODO: make this an env var
    $issuedAt = time();
    $expirationTime = $issuedAt + TOKEN_CONFIG['access_token_lifetime'];

    $payload = [
        'iat' => $issuedAt,
        'exp' => $expirationTime,
        'userId' => $userId,
        'role' => $role
    ];

    try {
        return JWT::encode($payload, $secretKey, 'HS256');
    } catch (Exception $e) {
        return null;
    }
}

function validateJwt($jwt) {
    // TODO: Move these to environment configuration
    $secretKey = "secret_key"; // TODO: make this an env var

    try {
        return JWT::decode($jwt, new Key($secretKey, 'HS256'));
    } catch (Exception $e) {
        respondError("Invalid token: " . $e->getMessage(), 401, false);
    }
}
