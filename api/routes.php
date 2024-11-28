<?php
$routes = [
    'task' => [
        'GET' => [
            '' => 'fetchTasks'       // For fetching all tasks
        ],
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
        ],
    ],
    'client' => [
        'GET' => [
            '' => 'getClients',
            'upcoming-events' => 'getUpcomingEvents',
            'menu-items' => 'getMenuItems',
            'email-domain' => 'getClientsByEmailDomain',
            'events' => 'getClientEvents'
        ],
        'POST' => [
            '' => 'createClient',
            'archive' => 'archiveClient',
            'notes' => 'updateClientNotes'
        ],
        'PUT' => [
            '' => 'updateClientDetails'
        ],
        'DELETE' => [
            '' => 'deleteClient'
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
    ]
];
?>