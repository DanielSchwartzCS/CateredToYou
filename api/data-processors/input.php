<?php
// api/data-processors/input.php
require_once __DIR__ . '/../utils/response.php';

function validateType($value, $type) {
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
            return false;
    }
}

function validateDate($date, $format) {
    $d = DateTime::createFromFormat($format, $date);
    return $d && $d->format($format) === $date;
}

function validateData($data, $requiredFields = []) {
    $validatedData = [];

    foreach ($requiredFields as $field => $type) {
        if (!array_key_exists($field, $data)) {
            respondError("Field '$field' is required", 400);
        }

        $value = $data[$field];

        if (!validateType($value, $type)) {
            respondError("Field '$field' must be of type '$type'", 400);
        }
        $validatedData[$field] = $value;
    }

    return $validatedData;
}

function getValidHttpBody($requiredFields = []) {
    $body = extractHttpBody();
    $validatedData = [];

    foreach ($body as $row) {
        $validatedRow = validateData($row, $requiredFields);
        $validatedData[] = $validatedRow;
    }

    return $validatedData;
}

function getValidQueryParams($requiredFields = []) {
    return validateData($_GET, $requiredFields);
}

function validateResourceSegments($segments, $requiredFields = []) {
    $validatedSegments = [];

    foreach ($requiredFields as $index => $type) {
        $value = $segments[$index] ?? null;

        if ($value === null) {
            respondError("Segment at index $index is required", 400);
        }

        if (!validateType($value, $type)) {
            respondError("Invalid segment at index $index. Expected type: $type", 400);
        }

        $validatedSegments[] = $value;
    }

    return $validatedSegments;
}
?>