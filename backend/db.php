<?php

$host = "localhost";
$db = "projetclsw";
$user = "*****";
$pass = "*****";

$dsn = "pgsql:host=$host;dbname=$db;user=$user;password=$pass";

function get_db() {
    global $dsn;
    try {
        $db = new PDO($dsn);
        $db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        return $db;
    } catch (PDOException $e) {
        echo $e->getMessage();
        exit;
    }
}