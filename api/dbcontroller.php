<?php
require_once 'response.php';

class DBController {
    public $conn;
    private $host = "143.110.237.101";
    private $database = "cateredtoyou";
    private $user = "root";
    private $password = "Dinosaur1234!!";
    private $dbh = null;

    function __construct() {
        $this->conn = $this->connectDB();
        if (!$this->conn) {
            respondWithError("Connection failed!", 500);
        }
    }

    function connectDB() {
        try {
            $dsn = "mysql:host=" . $this->host . ";port=3306;dbname=" . $this->database;
            $conn = new PDO($dsn, $this->user, $this->password);

            // Set PDO to throw exceptions in case of an error
            $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            return $conn;
        } catch (PDOException $e) {
            respondWithError("Failed to connect to the database: " . $e->getMessage(), 500);
        }
    }

    function __destruct() {
        $this->conn = null;
    }
}

function executeSelect($query, $params = [], $fetchMethod = PDO::FETCH_ASSOC) {
    $db = new DBController();
    try {
        $stmt = $db->conn->prepare($query);
        $stmt->execute($params);
    }
    catch(PDOException $e) {
        respondWithError("Failure to execute SELECT query: " . $e->getMessage(), 500);
    }
    return $fetchMethod == PDO::FETCH_ASSOC
        ? $stmt->fetchAll(PDO::FETCH_ASSOC)
        : $stmt->fetch(PDO::FETCH_ASSOC);
}

function executeInsert($query, $params = []) {
    $db = new DBController();
    try {
        $stmt = $db->conn->prepare($query);
        $stmt->execute($params);

        return $stmt->rowCount() > 0;
    }
    catch(PDOException $e) {
        respondWithError("Failure to execute INSERT query: " . $e->getMessage(), 500);
    }
    return false;
}


?>
