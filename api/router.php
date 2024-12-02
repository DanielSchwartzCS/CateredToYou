<?php
require_once 'response.php';
require_once 'routes.php';  // Include the routes configuration

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header("Access-Control-Allow-Credentials: true");
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Max-Age: 1000');
header('Access-Control-Allow-Headers: Origin, Content-Type, X-Auth-Token, Authorization');

$uri = trim($_SERVER['REQUEST_URI'], '/');
$method = $_SERVER['REQUEST_METHOD'];

$segments = explode('/', $uri);
$resource = array_shift($segments);

if (!isset($routes[$resource])) {
    respondWithError("Resource not found: $resource", 404);
}

if (!isset($routes[$resource][$method])) {
    respondWithError("HTTP Method not allowed for resource: $resource", 405);
}

if (isset($routes[$resource][$method][$segments[0]])) {
    $function = $routes[$resource][$method][$segments[0]];
} else {
    respondWithError("Sub-route not defined in resource: $resource", 405);
}

require_once "resources/$resource.php";

if (!function_exists($function)) {
    respondWithError("Function does not exist in resource: $resource", 405);
}

$function($segments);
?>