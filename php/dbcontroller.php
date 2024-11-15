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
?>

