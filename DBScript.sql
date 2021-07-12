CREATE DATABASE `customerDB`;
USE customerDB;
CREATE TABLE `dbuser` (
  `id` int NOT NULL AUTO_INCREMENT,
  `display_name` varchar(255) DEFAULT NULL,
  `mail` varchar(255) DEFAULT NULL,
  `mobile_number` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `token_store` (
  `user_id` int NOT NULL,
  `mfa_token` varchar(1000) DEFAULT NULL,
  `session_uuid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `mfa_user_mapping` (
  `user_id` int NOT NULL,
  `mfa_user_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


