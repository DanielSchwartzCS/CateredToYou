<?php
include 'db_connect.php';

if($_SERVER['REQUEST_METHOD'] == 'POST'){
	$firstname = $_POST['firstname'];
	$lastname = $_POST['lastname'];
	$email = $_POST['email'];
	$phonenumber = $_POST['phonenumber'];
	if(!empty($firstname) && !empty($lastname) && !empty($email) && !empty($phonenumber)){
		$stmt = $connection->prepare("INSERT INTO clients (firstname, lastname, email, phonenumber) VALUES (?, ?, ?, ?)");
		$stmt->bind_param("ssss", $firstname, $lastname, $email, $phonenumber);
		if($stmt->execute()){
			echo json_encode(["status" => true, "message" => "Successfully added a new client"]);
		}else{
			echo json_encode(["status" => false, "message" => "Unsuccessfully added a new client"]);
		}
		$stmt->close();
	}else{
		echo json_encode(["status" => false, "message" => "All fields must NOT be empty"]);
	}
}else{
	echo json_encode(["status" => false, "message" => "Invalid request method"]);
}
$connection->close();
?>
