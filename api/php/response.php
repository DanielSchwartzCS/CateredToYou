<?php

/**
 * Sends a JSON-encoded error response to the client with a specified HTTP status code and message.
 *
 * @param string $message The error message to be included in the response.
 * @param int $code The HTTP status code for the response (default is 400).
 * @return void
 */
function respondWithError($message, $code = 400) {
    http_response_code($code);
    echo json_encode(["status" => false, "message" => $message]);
    exit;
}

/**
 * Sends a JSON-encoded success response to the client with a specified HTTP status code,
 * message, and optional data payload.
 *
 * @param int $statusCode The HTTP status code for the response
 * @param string $message A descriptive success message to be included in the response.
 * @param mixed|null $data Optional. Additional data to include in the response. Provide tokens here
 *                         If provided, this data will be added under the "data" key.
 * @return void
 */
function respondWithSuccess($statusCode, $message, $data = null) {
    http_response_code($statusCode);
    $response = ["status" => true, "message" => $message];
    if ($data !== null) {
        $response['data'] = $data;
    }
    echo json_encode($response);
}
?>
