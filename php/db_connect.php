<?php
$servername = "sql3.freemysqlhosting.net";
$username = "sql3731131";
$password = "Dinosaur1234!!";
$dbname = "sql3731131";

// creates a connection and puts in in variable $connection
$connection = new mysqli($servername, $username, $password, $dbname);

// checks to see if connection is successful
if($connection->connect_error){
    die("Connection failed: " . $connection -> connect_error);
}
// want to see if this works :)
?>
