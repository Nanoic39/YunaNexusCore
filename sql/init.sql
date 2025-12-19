-- User Service Database
CREATE DATABASE IF NOT EXISTS `yuna_user` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `yuna_user`;

-- Placeholder table for User
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `username` varchar(64) DEFAULT NULL COMMENT 'Username Placeholder',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System User Placeholder';

-- Placeholder table for Role
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(64) DEFAULT NULL COMMENT 'Role Name Placeholder',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Role Placeholder';


-- File Service Database
CREATE DATABASE IF NOT EXISTS `yuna_file` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `yuna_file`;

-- Placeholder table for File
CREATE TABLE `sys_file` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `file_name` varchar(255) DEFAULT NULL COMMENT 'Filename Placeholder',
  `url` varchar(512) DEFAULT NULL COMMENT 'URL Placeholder',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='File Placeholder';


-- Log Service Database
CREATE DATABASE IF NOT EXISTS `yuna_log` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `yuna_log`;

-- Placeholder table for Logs
CREATE TABLE `sys_log_operation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `log_content` text COMMENT 'Log Content Placeholder',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Log Placeholder';


-- Auth Service Database
CREATE DATABASE IF NOT EXISTS `yuna_auth` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `yuna_auth`;

-- Placeholder table for OAuth Clients
CREATE TABLE `oauth_client` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `client_id` varchar(64) DEFAULT NULL COMMENT 'Client ID Placeholder',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OAuth Client Placeholder';
