<?php
require_once 'response.php';
require_once 'jwt.php';

// Function to store a refresh token in the database
function storeRefreshToken($userId, $token, $expiresAt) {
    // Cleanup expired tokens
    executeQuery("UPDATE refresh_tokens SET is_expired = TRUE WHERE expires_at < NOW()");

    // Insert new refresh token
    $successfulInserts = executeQuery("INSERT INTO refresh_tokens (user_id, token, expires_at, usage_count) VALUES (:user_id, :token, :expires_at, 0)", [
        ':user_id' => $userId,
        ':token' => $token,
        ':expires_at' => $expiresAt
    ]);

    // Check if the insert was successful
    if ($successfulInserts > 0) {
        respondWithSuccess("Token stored", 200);
    } else {
        respondWithError("Failed to store token:", 500);
    }
}

// Function to mark a refresh token as expired
function markTokenAsExpired($token) {
    return executeQuery("UPDATE refresh_tokens SET is_expired = TRUE WHERE token = :token", [':token' => $token]) > 0;
}

// Function to validate and refresh JWT based on refresh token
function refreshJwt($refreshToken) {
    $tokenData = executeQuery("SELECT user_id, expires_at, usage_count FROM refresh_tokens WHERE token = :token AND is_expired = FALSE",
        [':token' => $refreshToken]);

    if (!$tokenData) {
        respondWithError("Invalid or expired refresh token.", 401);
    }

    if (strtotime($tokenData['expires_at']) < time()) {
        respondWithError("Refresh token expired.", 401);
    }

    $usageLimit = 5;
    if ($tokenData['usage_count'] >= $usageLimit) {
        respondWithError("Refresh token has reached its usage limit.", 403);
    }

    executeQuery("UPDATE refresh_tokens SET usage_count = usage_count + 1 WHERE token = :token", [':token' => $refreshToken]);
    $user = executeQuery("SELECT role FROM users WHERE user_id = :user_id", [':user_id' => $tokenData['user_id']]);

    if (!$user) {
        respondWithError("User not found.", 404);
    }

    // Generate a new JWT for the user
    $newJwt = generateJwt($tokenData['user_id'], $user['role']);

    // Mark the refresh token as expired
    markTokenAsExpired($refreshToken);

    // Return the new JWT and expiration time
    respondWithSuccess(200, "Token refreshed successfully", [
        "token" => $newJwt,
        "expiresAt" => date('Y-m-d H:i:s', time() + 3600)
    ]);
}

// Function to validate the authorization header and extract user data
function validateAuthorization($header) {
    if (!$header) {
        return null;
    }

    $jwt = str_replace('Bearer ', '', $header);
    try {
        return JWT::decode($jwt, 'secret_key'); //TODO: store secret_key in env variables
    } catch (Exception $e) {
        if (isset($_POST['refreshToken'])) {
            $refreshToken = $_POST['refreshToken'];
            refreshJwt($refreshToken);
        } else {
            respondWithError("Invalid token and no refresh token provided.", 401);
        }
    }
}
?>
