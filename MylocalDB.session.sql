CREATE DATABASE IF NOT EXISTS flightdb DEFAULT CHARSET = utf8mb4;
USE flightdb;

CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(100) NOT NULL UNIQUE,
  full_name VARCHAR(100),
  password_hash VARCHAR(200) NOT NULL,
  role VARCHAR(20) DEFAULT 'user',
  disabled TINYINT(1) DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS flights (
  id INT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  origin VARCHAR(50),
  destination VARCHAR(50),
  depart_time DATETIME,
  arrive_time DATETIME,
  price DOUBLE,
  seats_total INT,
  seats_left INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  flight_id INT NOT NULL,
  seat_count INT,
  seat_number VARCHAR(50),
  order_time DATETIME,
  status VARCHAR(20) DEFAULT 'PENDING',
  pay_type VARCHAR(20) DEFAULT 'NONE',
  refund_request TINYINT(1) DEFAULT 0,
  reschedule_request TINYINT(1) DEFAULT 0,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (flight_id) REFERENCES flights(id)
);
USE testdb; -- 确保您正在操作正确的数据库

-- 创建航班表
CREATE TABLE flights (
    id INT PRIMARY KEY AUTO_INCREMENT,
    flight_code VARCHAR(10) NOT NULL UNIQUE,
    origin VARCHAR(50) NOT NULL,
    destination VARCHAR(50) NOT NULL,
    departure_time DATETIME NOT NULL,
    arrival_time DATETIME,
    price DECIMAL(10, 2) NOT NULL,
    total_seats INT NOT NULL,
    available_seats INT NOT NULL,
    flight_duration VARCHAR(20)
    ）；