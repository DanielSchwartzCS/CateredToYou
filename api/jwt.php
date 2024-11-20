<?php
require_once 'response.php';

require_once __DIR__ . '/vendor/autoload.php';

use \Firebase\JWT\JWT;

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
    try {
//         echo "Payload: ";
//         var_dump($payload);
	$tokenResponse = JWT::encode($payload, $secretKey, 'HS256');
// 	echo "generated";
        return $tokenResponse;
    } catch (Exception $e) {
        // Log the error for debugging
//         echo("Error generating JWT: " . $e->getMessage());
        return null;
    }

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
