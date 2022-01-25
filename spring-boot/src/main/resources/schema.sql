CREATE TABLE `dbuser` (
  `id` int AUTO_INCREMENT PRIMARY KEY,
  `display_name` VARCHAR(32) DEFAULT NULL,
  `mail` VARCHAR(64) DEFAULT NULL,
  `mobile_number` VARCHAR(15) DEFAULT NULL,
  `name` VARCHAR(32) DEFAULT NULL,
  `password` VARCHAR(255) DEFAULT NULL
);

CREATE TABLE `token_store` (
  `user_id` INT PRIMARY KEY,
  `mfa_token` VARCHAR(1000) DEFAULT NULL,
  `session_uuid` VARCHAR(128) DEFAULT NULL,
  `last_active_date_time` DATETIME DEFAULT NULL
);

CREATE TABLE `mfa_user_mapping` (
  `user_id` int NOT NULL PRIMARY KEY,
  `mfa_user_id` varchar(64) DEFAULT NULL
);