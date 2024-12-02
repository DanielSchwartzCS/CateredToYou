<?php
//api/utils/response.php
function respondSuccess($data = null, $statusCode = 200, $terminate = true) {
    http_response_code($statusCode);
    header('Content-Type: application/json');
    echo json_encode([
        'status' => 'success',
        'data' => $data
    ]);
    if ($terminate) exit;
}

function respondError($message, $statusCode = 400, $terminate = true) {
    http_response_code($statusCode);
    header('Content-Type: application/json');
    echo json_encode([
        'status' => 'error',
        'message' => $message
    ]);
    if ($terminate) exit;
}

?>