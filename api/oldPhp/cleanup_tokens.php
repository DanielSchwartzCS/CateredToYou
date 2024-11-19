<?php
require_once 'dbcontroller.php';

function cleanupExpiredTokens() {
    $db = new DBController();
    
    $stmt = $db->conn->prepare("DELETE FROM refresh_tokens WHERE is_expired = TRUE OR expires_at < NOW()");
    
    if ($stmt->execute()) {
        echo "Expired tokens cleaned up successfully.\n";
    } else {
        echo "Failed to clean up expired tokens: " . $stmt->errorInfo()[2] . "\n";
    }
}

cleanupExpiredTokens();
?>
