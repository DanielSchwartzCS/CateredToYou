<?php
include 'db_connect.php';
//checks to see that the request is a POST request
if($_SERVER['REQUEST_METHOD'] == 'POST'){
	$user = $_POST['username'];
	$pass = $_POST['password'];
	//checks to see if the post request variables are empty
	if(!empty($user) && !empty($pass)){
		$stmt = $connection->prepare("SELECT * FROM users WHERE username = ? AND password = ?");
		$stmt->bind_param("ss", $user, $pass);
		$stmt->execute();
		$result = $stmt->get_result();
		if($result->num_rows > 0){
			echo json_encode(["status" => true, "message" => "Login Successful"]);
		}else{
			echo json_encode(["status" => false, "message" => "Invalid username or password"]);
		}
		$stmt->close();
	}else{
		echo json_encode(["status" => false, "message" => "Username and Password cannot be empty"]);
	}
}else{
	echo json_encode (["status" => false, "message" => "Invalid request"]);
}
$connection->close();
?>

