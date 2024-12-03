<?php
require_once __DIR__ . '/utils/response.php';  // Correct the path for response.php
require_once __DIR__ . '/routes.php';
require_once __DIR__ . '/data-processors/input.php';
require_once __DIR__ . '/resources/auth.php'; // Handle token validation
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header("Access-Control-Allow-Credentials: true");
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Max-Age: 1000');
header('Access-Control-Allow-Headers: Origin, Content-Type, X-Auth-Token, Authorization');

// Development mode: bypass token validation
$devMode = true;

$uri = trim($_SERVER['REQUEST_URI'], '/');
$method = $_SERVER['REQUEST_METHOD'];

$segments = explode('/', $uri);
$resource = array_shift($segments);

if (!isset($routes[$resource])) {
    respondError("Resource not found: $resource", 404);
}
if (!isset($routes[$resource][$method])) {
    respondError("HTTP Method not allowed for resource: $resource", 405);
}

$subRoute = implode('/', $segments);
$routeKey = null;
$resourceId = null;

// Define public resources and methods
$publicResources = [
    'auth' => ['POST' => ['login', 'refresh']], // No token required for login/refresh
];

// Check if the requested endpoint is public
$isPublicEndpoint = isset($publicResources[$resource][$method]) &&
    in_array($subRoute, $publicResources[$resource][$method]);

if (!$devMode && !$isPublicEndpoint) {
    // Extract and validate token for private endpoints
    $authHeader = getallheaders()['Authorization'] ?? null;
    if (!$authHeader || !preg_match('/Bearer\s(\S+)/', $authHeader, $matches)) {
        respondError("Authorization token is missing or invalid.", 401);
    }

    $token = $matches[1];
    $userData = validateToken($token);
    if (!$userData) {
        respondError("Invalid or expired token.", 403);
    }

    $userRole = $userData['role'] ?? null;

    $permissions = null;
    if (isset($routes[$resource][$method])) {
        foreach ($routes[$resource][$method] as $pattern => $routeInfo) {
            $permissions = $routeInfo['roles'] ?? null;
            break;
        }
    }

    // Check if the user has the required role for the resource and method
    if ($permissions && !in_array($userRole, $permissions)) {
        respondError("Access denied for method $method on resource: $resource", 403);
    }

    if (is_bool($permissions)) {
        // Resource-wide access control
        if (!$permissions[$userRole]) {
            respondError("Access denied to resource: $resource", 403);
        }
    } elseif (is_array($permissions)) {
        // Method-specific access control
        $methodPermissions = $permissions[$userRole] ?? false;
        if (is_bool($methodPermissions) && !$methodPermissions) {
            respondError("Access denied to resource: $resource", 403);
        } elseif (is_array($methodPermissions) && !($methodPermissions[$method] ?? false)) {
            respondError("Access denied for method $method on resource: $resource", 403);
        }
    }
}

// Match the sub-route pattern
foreach ($routes[$resource][$method] as $pattern => $function) {

    $function = $function['handler'];

    if (preg_match_all('/\{([a-z_]+)\}/', $pattern, $matches)) {
        $routeRegex = preg_replace('/\{[a-z_]+\}/', '([^/]+)', $pattern);
        if (preg_match("#^$routeRegex$#", $subRoute, $params)) {
            $routeKey = $function;
            break;
        }
    } elseif ($pattern === $subRoute) {
        $routeKey = $function;
        break;
    }
}
if (!$routeKey) {
    respondError("Route not found: $subRoute", 404);
}

require_once "resources/$resource.php";
if (!function_exists($routeKey)) {
    respondError("Function does not exist: $routeKey", 405);
}

// Call the matched function with the validated resource ID
$routeKey($resourceId);
?>
