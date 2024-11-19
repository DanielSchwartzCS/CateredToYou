<?php
echo ("in router");
require_once 'response.php';

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header("Access-Control-Allow-Credentials: true");
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Max-Age: 1000');
header('Access-Control-Allow-Headers: Origin, Content-Type, X-Auth-Token, Authorization');
$uri = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);

$uri = str_replace('/php', '', $uri);
$segments = explode('/', trim($uri, '/'));
$method = $_SERVER['REQUEST_METHOD'];

// Get the first segment as the target file
$file = array_shift($segments);
$filePath = "{$file}.php";

//die("Requested URI: " . $uri . " ---  File path: " . $filePath);

// Check if the file exists
if (file_exists($filePath)) {
    require $filePath;
    // Call handleRequest in the file with the remaining segments
    if (function_exists('handleRequest')) {
        handleRequest($method, $segments);
    } else {
        respondWithError("Handler function not found in $filePath", 500);
    }
} else {
    respondWithError($filePath . " not found", 404);
}
?>
