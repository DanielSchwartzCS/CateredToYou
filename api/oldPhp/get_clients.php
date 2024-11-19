<?php
// Include database connection file
include 'db_connect.php';

if($_SERVER['REQUEST_METHOD'] == 'GET'){
	// Prepare the SQL query to fetch all clients
	$query = "SELECT id, firstname, lastname, email, phonenumber FROM clients";

	$result = $connection->query($query);

	$clients = array();

	if ($result->num_rows > 0) {
    		// Fetch each row and add to the clients array
    		while ($row = $result->fetch_assoc()) {
        		$clients[] = array(
            			"id" => $row["id"],
            			"firstname" => $row["firstname"],
            			"lastname" => $row["lastname"],
            			"email" => $row["email"],
            			"phonenumber" => $row["phonenumber"]
        		);
    		}
	}

	// Set header to JSON and output the clients array
	header('Content-Type: application/json');
	echo json_encode($clients);
}else{
	$error = "Invalid request method";
	echo json_ecode($error);
}
// Close the database connection
$connection->close();
?>
