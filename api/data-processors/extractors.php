<?php
//api/data-processors/extractors.php
function extractHttpBody() {
    $inputData = json_decode(file_get_contents('php://input'), true);

    if ($inputData === null) {
        respondError("Invalid JSON input", 400);
    }
    // Normalize single object to array of rows
    if (!is_array($inputData) || !is_array(reset($inputData))) {
        $inputData = [$inputData];
    }

    return $inputData;
}

function extractQueryParams() {
    return $_GET;
}

function extractResourceSegments($segments, $expectedCount) {
    if (count($segments) < $expectedCount) {
        respondError("Expected at least $expectedCount segments, got " . count($segments), 400);
    }

    return $segments;
}
?>