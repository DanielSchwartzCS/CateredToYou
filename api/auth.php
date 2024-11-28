<?php
require_once 'dbcontroller.php';
require_once 'response.php';
require_once 'jwt.php';
require_once 'token.php';
require_once 'validators.php';

// Handle login: authenticate user and return JWT
function login($segments) {
    $fieldsAndTypes = [
        'username' => 'string',
        'password' => 'string'
    ];

    $data = validateBody($fieldsAndTypes);
    $username = $data['username'];
    $password = $data['password'];

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
    $fieldsAndTypes = [
        'refresh_token' => 'string'
    ];

    $data = validateBody($fieldsAndTypes);
    $refreshToken = $data['refresh_token'];
    $userData = refreshJwt($refreshToken);

    $jwt = generateJwt($userData['userId'], $userData['role']);

    respondWithSuccess("JWT refreshed successfully", 200, ['jwt' => $jwt]);
}

/**
 * Handle logout: marks the refresh token as expired in the database
 *
 * Expected Behavior:
 * - Marks the provided refresh token as expired in the database, making it
 *   invalid for future authentication requests. Responds with a success
 *   message upon completion.
 *
 * What it doesn't do:
 * - Does NOT remove or invalidate the JWT on the client side. The client
 *   application is responsible for deleting the JWT from local storage,
 *   session, or cookies.
 * - Does NOT affect any active sessions on the client side. The front-end
 *   must manage the token state after this request (e.g., removing the
 *   JWT from storage or session).
 * - Does NOT revoke other active sessions or tokens. It only expires the
 *   refresh token provided in the request.
 */
function logout($segments) {
    $fieldsAndTypes = [
        'refresh_token' => 'string'
    ];

    $data = validateBody($fieldsAndTypes);
    $refreshToken = $data['refresh_token'];

    markTokenAsExpired($refreshToken);

    respondWithSuccess("Logged out successfully", 200);
}

?>