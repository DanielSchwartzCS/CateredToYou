<?php
$routes = [
    'task' => [
        'GET' => [
            '' => 'fetchTasks',       // For fetching all or filtered tasks
            'event' => 'fetchTasksByEvent'
        ],
        'PATCH' => [
            '{task_id}' => 'updateStatus'
        ],
        'PUT' => [
            '{task_id}' => 'createTask'
        ]
    ],
    'auth' => [
        'POST' => [
            '' => 'login',        // For login
            'refresh' => 'refresh',  // For refreshing JWT
            'logout' => 'logout'    // For logging out
        ]
    ],
    'token' => [
        'POST' => [
            'refresh' => 'refreshJwt',  // For refreshing JWT using a refresh token
            'logout' => 'markTokenAsExpired'  // For logging out and marking token expired
        ]
    ],
    'client' => [
        'GET' => [
            '' => 'getClients',                  // Fetch all clients
            'upcoming-events' => 'getUpcomingEvents'  // Fetch clients with upcoming events
        ],
        'POST' => [
            '' => 'createClient',               // Create a new client
            'notes/{client_id}' => 'updateClientNotes' // Update notes for a specific client
        ],
        'PUT' => [
            '{client_id}' => 'updateClientDetails'         // Update client details (client ID in segments)
        ],
        'PATCH' => [
            'notes/{client_id}' => 'patchClientNotes'       // Partial update of client notes (client ID in segments)
        ]
    ],
    'event' => [
        'GET' => [
            '' => 'fetchEvents'  // For fetching all events or a specific event
        ],
        'POST' => [
            '' => 'createEvent'  // For creating a new event
        ],
        'PUT' => [
            '' => 'updateEvent'  // For updating an event
        ],
        'DELETE' => [
            '' => 'deleteEvent'  // For deleting an event
        ]
    ],
    'menu' => [
        'GET' => [
            '' => 'fetchMenuItems',  // Fetch all menu items
            'event' => 'fetchEventMenuItems'  // Fetch menu items by event ID
        ],
        'POST' => [
            '' => 'createMenuItem'  // Create a new menu item
        ],
        'PUT' => [
            '' => 'updateMenuItem'  // Update an existing menu item
        ],
        'DELETE' => [
            '' => 'deleteMenuItem'  // Delete an existing menu item
        ]
    ],
    'inventory' => [
        'GET' => [
            '' => 'fetchAllInventory',  // Fetch all inventory items
            'event' => 'fetchInventoryByEvent'  // Fetch inventory related to a specific event
        ],
        'POST' => [
            '' => 'createInventory'  // Create new inventory item(s)
        ],
        'PUT' => [
            '' => 'updateInventoryEntry'  // Update entire inventory entry
        ],
        'PATCH' => [
            '{inventory_id}' => 'updateInventoryQuantity'  // Update quantity of an inventory item
    ]
];
?>