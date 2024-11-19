<?php
require_once 'dbcontroller.php';
require_once 'response.php';
require_once 'jwt.php';

// Function to store a refresh token in the database
function storeRefreshToken($userId, $token, $expiresAt) {
    $db = new DBController();
    $cleanupStmt = $db->conn->prepare("UPDATE refresh_tokens SET is_expired = TRUE WHERE expires_at < NOW()");
    $cleanupStmt->execute();

    $stmt = $db->conn->prepare("INSERT INTO refresh_tokens (user_id, token, expires_at, usage_count) VALUES (:user_id, :token, :expires_at, 0)");
    $stmt->bindParam(':user_id', $userId, PDO::PARAM_INT);
    $stmt->bindParam(':token', $token, PDO::PARAM_STR);
    $stmt->bindParam(':expires_at', $expiresAt, PDO::PARAM_STR);

    if ($stmt->execute()) {
        return true;
    } else {
        respondWithError("Failed to store token: " . $stmt->errorInfo()[2], 500);
    }
}

// Function to mark a refresh token as expired
function markTokenAsExpired($token) {
    $db = new DBController();
    $stmt = $db->conn->prepare("UPDATE refresh_tokens SET is_expired = TRUE WHERE token = :token");
    $stmt->bindParam(':token', $token, PDO::PARAM_STR);
    $stmt->execute();
}

// Function to validate and refresh JWT based on refresh token
function refreshJwt($refreshToken) {
    $db = new DBController();
    $stmt = $db->conn->prepare("SELECT user_id, expires_at, usage_count FROM refresh_tokens WHERE token = :token AND is_expired = FALSE");
    $stmt->bindParam(':token', $refreshToken, PDO::PARAM_STR);
    $stmt->execute();
    $tokenData = $stmt->fetch(PDO::FETCH_ASSOC);

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

    $updateStmt = $db->conn->prepare("UPDATE refresh_tokens SET usage_count = usage_count + 1 WHERE token = :token");
    $updateStmt->bindParam(':token', $refreshToken, PDO::PARAM_STR);
    $updateStmt->execute();

    $stmt = $db->conn->prepare("SELECT role FROM users WHERE user_id = :user_id");
    $stmt->bindParam(':user_id', $tokenData['user_id'], PDO::PARAM_INT);
    $stmt->execute();
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$user) {
        respondWithError("User not found.", 404);
    }

    $newJwt = generateJwt($tokenData['user_id'], $user['role']);
    markTokenAsExpired($refreshToken);

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
        return JWT::decode($jwt, 'secret_key');
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
