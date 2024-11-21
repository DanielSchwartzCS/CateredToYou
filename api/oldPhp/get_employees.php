<?php
// uses DB_Connect.php to connect to database
include 'db_connect.php';
// query
$sql = "SELECT * FROM employee";
// executes query and puts it into $result
$result = $connection -> query($sql);
// a data array for the tables that will be returned
$data = array();
// check if it returns anything
if($result -> num_rows > 0){
    // loop through each record and add it to the array
    while($row = $result -> fetch_assoc()){
        $data[] = $row;
    }
}
// turns it into a json file and returns it
echo json_encode($data);
//closes the connection
$connection -> close();
?>
