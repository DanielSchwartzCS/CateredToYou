<?php
// api/resources/auth.php
require_once __DIR__ . '/../database/dbcontroller.php';
require_once __DIR__ . '/../utils/response.php';
require_once __DIR__ . '/../utils/jwt.php';
require_once __DIR__ . '/../utils/token.php';
require_once __DIR__ . '/../data-processors/input.php';

function login() {
    $requiredFields = [
        'username' => 'string',
        'password' => 'string'
    ];

    $data = getValidHttpBody($requiredFields)[0];
    $username = $data['username'];
    $password = $data['password'];

    $userData = executeSelect(
        "SELECT user_id, password_hash, role FROM users WHERE username = :username",
        [':username' => $username],
        false
    );

    if ($userData && password_verify($password, $userData['password_hash'])) {
        $jwt = generateJwt($userData['user_id'], $userData['role']);

        // Create and store refresh token
        $refreshToken = bin2hex(random_bytes(32));
        $expiresAt = date('Y-m-d H:i:s', time() + 86400); // 1-day expiration for refresh token
        storeRefreshToken($userData['user_id'], $refreshToken, $expiresAt);

        respondSuccess([
            'jwt' => $jwt,
            'refresh_token' => $refreshToken
        ]);
    } else {
        respondError("Invalid credentials", 401);
    }
}

function refresh() {
    $requiredFields = [
        'refresh_token' => 'string'
    ];

    $data = getValidHttpBody($requiredFields)[0];
    $refreshToken = $data['refresh_token'];
    $userData = refreshJwt($refreshToken);

    $jwt = generateJwt($userData['userId'], $userData['role']);

    respondSuccess(['jwt' => $jwt]);
}

function logout() {
    $requiredFields = [
        'refresh_token' => 'string'
    ];

    $data = getValidHttpBody($requiredFields)[0];
    $refreshToken = $data['refresh_token'];

    markTokenAsExpired($refreshToken);

    respondSuccess(null, 204);
}
?>