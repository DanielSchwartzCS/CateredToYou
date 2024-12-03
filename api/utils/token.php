<?php
// api/utils/token.php
require_once __DIR__ . '/../database/dbcontroller.php';
require_once __DIR__ . '/../utils/jwt.php';

// CONFIGURATION: Token lifecycle parameters
const TOKEN_CONFIG = [
    'access_token_lifetime' => 3600,        // 1 hour
    'refresh_token_lifetime' => 2592000,    // 30 days
    'max_refresh_cycles' => 20,             // Maximum number of refresh cycles before mandatory re-login
    'sliding_window_extension' => 1209600   // 14 days - extends session if actively used
];

// FUNCTION SIG CHANGED
function storeRefreshToken($userId, $role) {
    // Generate a more robust refresh token
    $token = bin2hex(random_bytes(32));
    $now = time();
    $expiresAt = date('Y-m-d H:i:s', $now + TOKEN_CONFIG['refresh_token_lifetime']);

    // Store extended token information
    $insertResult = executeInsert("INSERT INTO refresh_tokens (
        user_id,
        token,
        created_at,
        last_used_at,
        expires_at,
        refresh_count,
        role,
        is_expired
    ) VALUES (
        :user_id,
        :token,
        :created_at,
        :last_used_at,
        :expires_at,
        :refresh_count,
        :role,
        FALSE
    )", [
        ':user_id' => $userId,
        ':token' => $token,
        ':created_at' => date('Y-m-d H:i:s', $now),
        ':last_used_at' => date('Y-m-d H:i:s', $now),
        ':expires_at' => $expiresAt,
        ':refresh_count' => 0,
        ':role' => $role
    ]);

    return $token;
}

function expireOldTokens($userId) {
    return executeChange("UPDATE refresh_tokens SET is_expired = TRUE WHERE user_id = :user_id AND is_expired = FALSE", [
        ':user_id' => $userId
    ]);
}

function markTokenAsExpired($token) {
    return executeChange("UPDATE refresh_tokens SET is_expired = TRUE WHERE token = :token", [':token' => $token]);
}

// FUNCTION SIG CHANGED
function refreshJwt($refreshToken) {
    // Retrieve token details
    $tokenData = executeSelect("
        SELECT
            user_id,
            role,
            created_at,
            last_used_at,
            refresh_count,
            expires_at
        FROM refresh_tokens
        WHERE token = :token AND is_expired = FALSE
    ", [':token' => $refreshToken], false);

    if (!$tokenData) {
        respondError("Invalid refresh token", 401);
    }

    $now = time();
    $createdAt = strtotime($tokenData['created_at']);
    $lastUsedAt = strtotime($tokenData['last_used_at']);
    $expiresAt = strtotime($tokenData['expires_at']);

    // Check refresh token lifecycle
    if ($now > $expiresAt) {
        respondError("Refresh token expired. Please log in again.", 401);
    }

    // Check refresh count
    if ($tokenData['refresh_count'] >= TOKEN_CONFIG['max_refresh_cycles']) {
        respondError("Maximum refresh cycles reached. Please log in again.", 403);
    }

    // Optional: Extend session if used within sliding window
    $shouldExtendSession =
        ($now - $createdAt) < TOKEN_CONFIG['sliding_window_extension'] ||
        ($now - $lastUsedAt) < TOKEN_CONFIG['sliding_window_extension'];

    // Mark old token as expired
    markTokenAsExpired($refreshToken);

    // Generate new tokens
    $newRefreshToken = storeRefreshToken($tokenData['user_id'], $tokenData['role']);
    $newAccessToken = generateJwt($tokenData['user_id'], $tokenData['role']);

    return [
        'userId' => $tokenData['user_id'],
        'role' => $tokenData['role']
    ];
}

function validateAuthorization($header) {
    if (!$header) {
        return null;
    }

    // Remove 'Bearer ' prefix
    $jwt = str_replace('Bearer ', '', $header);

    try {
        return validateJwt($jwt);
    } catch (Exception $e) {
        respondError("Invalid token.", 401);
    }
}