<?php
require_once 'response.php';
require_once 'auth.php';

// Check if it's a POST request
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $data = json_decode(file_get_contents("php://input"), true);

    if (isset($data['refreshToken'])) {
        $refreshToken = $data['refreshToken'];
        $newAccessToken = refreshJwt($refreshToken);

        if ($newAccessToken) {
            echo json_encode(['accessToken' => $newAccessToken]);
        } else {
            respondWithError("Unable to refresh the token", 401);
        }
    } else {
        respondWithError("Refresh token is required", 400);
    }
} else {
    respondWithError("Invalid request method", 405);
}

?>
