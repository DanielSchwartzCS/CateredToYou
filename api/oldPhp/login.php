<?php
require_once 'auth.php';
require_once 'dbcontroller.php';
require_once 'response.php';


function handleRequest($method, $segments) {
    if ($method == 'POST') {
        handleLogin();
    } else {
        respondWithError("Method not allowed", 405);
    }
}

// Handle Login action
function handleLogin() {
    $input = json_decode(file_get_contents("php://input"), true);
    $username = $input['username'] ?? null;
    $password = $input['password'] ?? null;

    if (empty($username) || empty($password)) {
        respondWithError("Username and password are required", 400);
    }

    // Query to check user credentials
    $db = new DBController();
    $stmt = $db->conn->prepare("SELECT user_id, password_hash, role FROM users WHERE username = :username");
    $stmt->bindParam(':username', $username, PDO::PARAM_STR);
    $stmt->execute();
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($user && password_verify($password, $user['password_hash'])) {
        error_log("made it to line 33");
        $token = generateJwt($user['user_id'], $user['role']);
        error_log("Generated token: " . $token);
        $refreshToken = bin2hex(random_bytes(32));
        $expiresAt = date('Y-m-d H:i:s', time() + 86400); // 1-day expiration for refresh token

        storeRefreshToken($user['user_id'], $refreshToken, $expiresAt);

        respondWithSuccess(200, "Login successful", [
            "token" => $token,
            "refreshToken" => $refreshToken
        ]);
    } else {
        respondWithError("Invalid username or password", 401);
    }
}
?>

