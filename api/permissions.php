<?php

// Role-based access control configuration
$resourcePermissions = [
    'client' => [
        'employee' => false,
        'caterer' => true // 100% off-limits to employees
    ],
    'task' => [
        'employee' => ['GET' => true, 'PATCH' => true], // Limited access for employees
        'caterer' => true // Full access for caterers
    ],
    'menu' => [
        'employee' => false,
        'caterer' => true // Full access for caterers
    ],
    // Add more resources as needed
];

?>
