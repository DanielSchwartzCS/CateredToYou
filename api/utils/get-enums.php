<?php
// src/utils/enum-updater.php

require_once __DIR__ . '/../database/dbcontroller.php';
require_once __DIR__ . '/../utils/response.php';

function updateEnumFile() {
    $enumData = [];

    try {
        // Query all columns with ENUM type
        $query = "
            SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = :database AND DATA_TYPE = 'enum'
        ";

        // Execute the query using executeSelect method
        $db = new DBController();
        $columns = executeSelect($query, [':database' => $db->database]);

        foreach ($columns as $column) {
            $table = $column['TABLE_NAME'];
            $field = $column['COLUMN_NAME'];

            // Extract ENUM values
            preg_match('/^enum\((.*)\)$/', $column['COLUMN_TYPE'], $matches);
            $enumValues = array_map(function ($val) {
                return trim($val, "'");
            }, explode(',', $matches[1]));

            $enumData[$table][$field] = $enumValues;
        }

        // Generate PHP code for the enums file
        $phpCode = "<?php\n\nreturn " . var_export($enumData, true) . ";\n";

        $filePath = __DIR__ . '/../data-processors/enum.php';
        file_put_contents($filePath, $phpCode);

        //echo "Enum file successfully updated: $filePath\n";
    } catch (Exception $e) {
        respondError("Failed to update enum file: " . $e->getMessage(), 500);
    }
}
?>