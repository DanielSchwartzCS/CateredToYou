<?php
include 'db_connect.php';
// checks to see if the request is a POST request
if($_SERVER['REQUEST_METHOD'] == 'POST'){
    $user = $_POST['username'];
    $pass = $_POST['password'];
    // checks to see if the post request variables are empty
    if(!empty($user) && !empty($pass)){
        $stmt = $connection->prepare("INSERT INTO employee (username, pass) VALUES (?, ?)");
        $stmt->bind_param("ss", $user, $pass);
	//executes the query
        if($stmt->execute()){
            echo json_encode(array("status" => "success", "message" => "User is registered"));
        }else{
            echo json_encode(array("status" => "failure", "message" => "Error registering user: " . $stmt->error));
        }
	//lots of error messages
        $stmt->close();
    }else {
        echo json_encode(array("status" => "failure", "message" => "username and password cannot be empty"));
    }

}else{
    echo json_encode(array("status" => "failure", "message" => "Invalid request method"));
}
$connection->close();
?>
