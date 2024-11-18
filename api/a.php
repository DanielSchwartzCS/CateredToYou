<?php
require_once 'jwt.php';
require_once 'auth.php';

echo 'Hook test 8';

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

// Login logic
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $input = json_decode(file_get_contents("php://input"), true);
    $username = $input['username'] ?? null;
    $password = $input['password'] ?? null;

    if (empty($username) || empty($password)) {
        echo("yo put in user/pw");
    }

    // Query to check user credentials
try {
    $db = new DBController();
    echo "DBController instantiated\n";
    echo "Preparing SQL query: SELECT user_id, password_hash, role FROM users WHERE username = :username\n";
    $stmt = $db->conn->prepare("SELECT user_id, password_hash, role FROM users WHERE username = :username");
    echo "Binding parameter: :username with value $username\n";
    $stmt->bindParam(':username', $username, PDO::PARAM_STR);
    echo "Executing the query...\n";
    $stmt->execute();
    echo "Query executed successfully\n";
    echo "Fetching query results...\n";
    $user = $stmt->fetch(PDO::FETCH_ASSOC);
    if ($user) {
        echo "User found: " . json_encode($user) . "\n";
    } else {
        echo "No user found for username: $username\n";
    }
} catch (PDOException $e) {
    echo "PDOException caught: " . $e->getMessage() . "\n";
}
    if ($user && password_verify($password, $user['password_hash'])) {
        $token = generateJwt($user['user_id'], $user['role']);
        $refreshToken = bin2hex(random_bytes(32));
        $expiresAt = date('Y-m-d H:i:s', time() + 86400); // 1-day expiration for refresh token
	echo 'attempingt ot store token';
        storeRefreshToken($user['user_id'], $refreshToken, $expiresAt);
	echo 'stored';

    } else {
echo("uhoh");
    }
}
?>
