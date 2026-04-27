-- Bootstrap do banco de testes para o profile `test`
-- Execute com um usuário administrativo (ex.: root no container MariaDB)

CREATE DATABASE IF NOT EXISTS brewer_test
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'brewer'@'%' IDENTIFIED BY 'brewer';
CREATE USER IF NOT EXISTS 'brewer'@'localhost' IDENTIFIED BY 'brewer';

GRANT ALL PRIVILEGES ON brewer_test.* TO 'brewer'@'%';
GRANT ALL PRIVILEGES ON brewer_test.* TO 'brewer'@'localhost';

FLUSH PRIVILEGES;
