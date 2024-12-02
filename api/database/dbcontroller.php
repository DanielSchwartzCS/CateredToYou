<?php
//api/database/dbcontroller.php
require_once 'response.php';

/**
 * Database Controller Class
 * Manages the connection to the database using PDO and ensures proper cleanup.
 */
class DBController {
    public $conn; // Public PDO connection object
    private $host = "143.110.237.101"; // Database host
    private $database = "cateredtoyou"; // Database name
    private $user = "root"; // Database username
    private $password = "Dinosaur1234!!"; // Database password

    /**
     * Constructor
     * Initializes the database connection and checks if the connection is successful.
     */
    function __construct() {
        $this->conn = $this->connectDB();
        if (!$this->conn) {
            respondError("Connection failed!", 500);
        }
    }

    /**
     * Connects to the database using PDO.
     *
     * @return PDO|null The PDO connection object or null if the connection fails.
     */
    function connectDB() {
        try {
            $dsn = "mysql:host=" . $this->host . ";port=3306;dbname=" . $this->database;
            $conn = new PDO($dsn, $this->user, $this->password);

            // Set PDO to throw exceptions in case of an error
            $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            return $conn;
        } catch (PDOException $e) {
            respondError("Failed to connect to the database: " . $e->getMessage(), 500);
        }
    }

    /**
     * Destructor
     * Ensures that the database connection is closed when the object is destroyed.
     */
    function __destruct() {
        $this->conn = null;
    }
}

/**
 * Executes a SELECT query on the database.
 *
 * @param string $query The SQL query to execute.
 * @param array $params Optional parameters to bind to the query.
 * @param bool $multipleRows If true, fetches all rows; if false, fetches one row.
 * @return array|null The result set as an associative array (or null if no rows found).
 */
function executeSelect($query, $params = [], $multipleRows = true) {
    $db = new DBController();
    try {
        $stmt = $db->conn->prepare($query);
        $stmt->execute($params);
    } catch (PDOException $e) {
        respondError("Failure to execute SELECT query: " . $e->getMessage(), 500);
    }
    return $multipleRows
        ? $stmt->fetchAll(PDO::FETCH_ASSOC)
        : $stmt->fetch(PDO::FETCH_ASSOC);
}

/**
 * Executes an UPDATE or DELETE query on the database.
 *
 * @param string $query The SQL query to execute.
 * @param array $params Optional parameters to bind to the query.
 * @return int|false The number of affected rows, or false on failure.
 */
function executeChange($query, $params = []) {
    $db = new DBController();
    try {
        $stmt = $db->conn->prepare($query);
        $stmt->execute($params);
        return $stmt->rowCount();
    } catch (PDOException $e) {
        respondError("Failure to execute query: " . $e->getMessage(), 500);
    }
}

/**
 * Executes an INSERT query and returns the last inserted ID.
 *
 * @param string $query The SQL INSERT query to execute.
 * @param array $params Parameters to bind to the query.
 * @return int|false The last inserted ID, or false on failure.
 */
function executeInsert($query, $params = []) {
    $db = new DBController();
    try {
        $stmt = $db->conn->prepare($query);
        $stmt->execute($params);
        return $db->conn->lastInsertId();
    } catch (PDOException $e) {
        respondError("Failure to execute INSERT query: " . $e->getMessage(), 500);
        return null;
    }
}
?>