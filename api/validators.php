<?php
require_once 'response.php';

function validate($value, $type) {
    switch ($type) {
        case 'posInt':
            return ctype_digit($value) && (int)$value > 0;
        case 'nonNegInt':
            return ctype_digit($value) && (int)$value >= 0;
        case 'int':
            return filter_var($value, FILTER_VALIDATE_INT) !== false;
        case 'float':
            return filter_var($value, FILTER_VALIDATE_FLOAT) !== false;
        case 'string':
            return is_string($value) && trim($value) !== '';
        case 'email':
            return filter_var($value, FILTER_VALIDATE_EMAIL) !== false;
        case 'date':
            return validateDate($value, 'Y-m-d');
        case 'time':
            return validateDate($value, 'H:i:s');
        case 'datetime':
            return validateDate($value, 'Y-m-d H:i:s');
        default:
            return null;
    }
}

//find more formats at https://www.php.net/manual/en/datetimeimmutable.createfromformat.php
function validateDate($date, $format) {
    $d = DateTime::createFromFormat($format, $date);
    if ($d && $d->format($format) === $date) {
        // Return the date in MySQL format: 'Y-m-d H:i:s'
        return $d->format('Y-m-d H:i:s');
    }
    return null; // Invalid date
}

//index is position of resource to validate in segments
function validateResource($segments, $index, $type = 'posInt') {
    $value = $segments[$index] ?? null;

    if ($value === null){
        respondWithError("Resource for validation is required.", 400);
    }
    if (!validate($value, $type)) {
        respondWithError("Invalid segment at index $index. Expected type: $type.", 400);
    }

    return $value;
}

function validateQueryParam($param, $type) {
    $value = $_GET[$param] ?? null;

    if ($value === null) {
        respondWithError("Query parameter $param is required", 400);
    }

    if (validate($value, $type)) {
        respondWithError("Query parameter $param must be of type $type", 400);
    }

    return $value;
}

function validateBody($fieldsAndTypes) {
    $inputData = json_decode(file_get_contents('php://input'), true);

    if ($inputData === null) {
        respondWithError("Invalid JSON input", 400);
    }

    if (!is_array($inputData) || !is_array(reset($inputData))) {
        $inputData = [$inputData];
    }

    foreach ($inputData as $row) {
        validateRow($row, $fieldsAndTypes);
    }

    return $inputData;
}

function validateRow($row, $fieldsAndTypes) {
    foreach ($fieldsAndTypes as $field => $type) {
        if (!array_key_exists($field, $row)) {
            respondWithError("Field $field is required", 400);
        }

        if (gettype($row[$field]) !== $type) {
            respondWithError("Field $field should be of type $type", 400);
        }
    }
}
?>
