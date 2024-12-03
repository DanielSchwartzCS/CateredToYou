<?php
// api/routes.php
$routes = [
    'auth' => [
        'POST' => [
            '' => [                    // For login
                'handler' => 'login',
                'roles'   => ['guest', 'employee', 'caterer']
            ],
            'refresh' => [             // For refreshing JWT
                'handler' => 'refresh',
                'roles'   => ['employee', 'caterer']
            ],
            'logout' => [              // For logging out
                'handler' => 'logout',
                'roles'   => ['employee', 'caterer']
            ]
        ]
    ],
    'client' => [
        'GET' => [
            '' => [                    // Fetch all clients
                'handler' => 'fetchAllClients',
                'roles'   => ['caterer']
            ],
            '{client_id}' => [         // Fetch client by id
                'handler' => 'fetchClientById',
                'roles'   => ['caterer']
            ]
        ],
        'POST' => [
            '' => [                    // Create a new client
                'handler' => 'createClient',
                'roles'   => ['caterer']
            ],
            'notes/{client_id}' => [   // Update notes for a specific client
                'handler' => 'updateClientNotes',
                'roles'   => ['caterer']
            ]
        ],
        'PUT' => [
            '{client_id}' => [         // Update client details (client ID in segments)
                'handler' => 'updateClientDetails',
                'roles'   => ['caterer']
            ]
        ],
        'PATCH' => [
            'notes/{client_id}' => [   // Partial update of client notes (client ID in segments)
                'handler' => 'patchClientNotes',
                'roles'   => ['caterer']
            ]
        ]
    ],
    'event' => [
        'GET' => [
            '' => [                    // For fetching all events
                'handler' => 'fetchEvents',
                'roles'   => ['employee', 'caterer']
            ],
            '{event_id}' => [          // For fetching a specific event
                'handler' => 'fetchEventById',
                'roles'   => ['employee', 'caterer']
            ],
            'client/{client_id}' => [  // Fetch events related to a specific client
                'handler' => 'fetchClientEvents',
                'roles'   => ['caterer']
            ]
        ],
        'POST' => [
            '' => [                    // For creating a new event
                'handler' => 'createEvent',
                'roles'   => ['caterer']
            ]
        ],
        'PUT' => [
            '' => [                    // For updating an event
                'handler' => 'updateEvent',
                'roles'   => ['caterer']
            ]
        ]
    ],
    'inventory' => [
        'GET' => [
            '' => [                    // Fetch all inventory items
                'handler' => 'fetchAllInventory',
                'roles'   => ['caterer']
            ],
            '{inventory_id}' => [      // Fetch a specific inventory item
                'handler' => 'fetchInventoryById',
                'roles'   => ['caterer']
            ],
            'event/{event_id}' => [    // Fetch inventory related to a specific event
                'handler' => 'fetchInventoryByEvent',
                'roles'   => ['caterer']
            ]
        ],
        'POST' => [
            '' => [                    // Create a new inventory item
                'handler' => 'createInventory',
                'roles'   => ['caterer']
            ]
        ],
        'PUT' => [
            '' => [                    // Update an entire inventory entry
                'handler' => 'updateInventoryEntry',
                'roles'   => ['caterer']
            ]
        ],
        'PATCH' => [
            '{inventory_id}' => [      // Update the quantity of a specific inventory item
                'handler' => 'updateInventoryQuantity',
                'roles'   => ['caterer']
            ]
        ]
    ],
    'menu' => [
        'GET' => [
            '' => [                    // Fetch all menu items
                'handler' => 'fetchMenuItems',
                'roles'   => ['caterer']
            ],
            'event/{event_id}' => [    // Fetch menu items for a specific event
                'handler' => 'fetchEventMenuItems',
                'roles'   => ['employee', 'caterer']
            ]
        ],
        'POST' => [
            '' => [                    // Create a new menu item
                'handler' => 'createMenuItem',
                'roles'   => ['caterer']
            ]
        ],
        'PUT' => [
            '' => [                    // Update an existing menu item
                'handler' => 'updateMenuItem',
                'roles'   => ['caterer']
            ]
        ]
    ],
    'task' => [
        'GET' => [
            '' => [                    // For fetching all or filtered tasks
                'handler' => 'fetchTasks',
                'roles'   => ['employee', 'caterer']
            ],
            '{event_id}' => [          // Fetch tasks by event ID
                'handler' => 'fetchTasksByEvent',
                'roles'   => ['employee', 'caterer']
            ]
        ],
        'PATCH' => [
            '{task_id}' => [           // For updating task status
                'handler' => 'updateStatus',
                'roles'   => ['employee', 'caterer']
            ]
        ],
        'PUT' => [
            '{task_id}' => [           // For creating a new task
                'handler' => 'createTask',
                'roles'   => ['caterer']
            ]
        ]
    ],
    'token' => [
        'POST' => [
            'refresh' => [             // For refreshing JWT using a refresh token
                'handler' => 'refreshJwt',
                'roles'   => ['employee', 'caterer']
            ],
            'logout' => [              // For logging out and marking token expired
                'handler' => 'markTokenAsExpired',
                'roles'   => ['employee', 'caterer']
            ]
        ]
    ],
    'user' => [
        'GET' => [
            '' => [                    // Fetch all users
                'handler' => 'fetchAllUsers',
                'roles'   => ['caterer']
            ],
            '{user_id}' => [           // Fetch user by ID
                'handler' => 'fetchUserById',
                'roles'   => ['caterer']
            ]
        ],
        'POST' => [
            '' => [                    // Create a new user
                'handler' => 'createUser',
                'roles'   => ['caterer']
            ]
        ],
        'PUT' => [
            '{user_id}' => [           // Update user details by ID
                'handler' => 'updateUserDetails',
                'roles'   => ['caterer']
            ],
            'role/{user_id}' => [      // Update user role by ID
                'handler' => 'updateUserRole',
                'roles'   => ['caterer']
            ],
            'password/{user_id}' => [  // Update user password by ID
                'handler' => 'updatePassword',
                'roles'   => ['caterer']
            ],
            'employment_status/{user_id}' => [  // Update employment status by ID
                'handler' => 'updateEmploymentStatus',
                'roles'   => ['caterer']
            ]
        ]
    ]
];
?>