<?php
require_once "db.php";

$data = json_decode(file_get_contents('php://input'), true);

if ($data["clsw"]) {
    $sensor = $data["sensor"];
    $value = $data["value"];

    $db = get_db();

    $query = "INSERT INTO data (sensor, value, origin) VALUES (:sensor, :value, 'projet1')";
    $stmt = $db->prepare($query);
    $stmt->bindValue(":sensor", $sensor, PDO::PARAM_STR);
    $stmt->bindValue(":value", json_encode($value), PDO::PARAM_STR);
    
    try {
        $stmt->execute();
    } catch (PDOException $e) {
        echo $e->getMessage();
        exit;
    }
    http_response_code(200);
    echo "OK";
    exit;
}

http_response_code(400);