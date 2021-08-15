# --- !Ups
CREATE TABLE `customer` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `first_name` VARCHAR(50) NOT NULL,
    `last_name` VARCHAR(50) NOT NULL,
    `postal_code` INTEGER NOT NULL,
    `address` VARCHAR(50) NOT NULL,
    `phone_number` INTEGER NOT NULL,
    `email` VARCHAR(50) NOT NULL,
    `password` VARCHAR(50) NOT NULL,
    `date_of_birth` DATE,
    `gender` SMALLINT  NOT NULL,
    `registered_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `delete_flg` SMALLINT NOT NULL DEFAULT 0,
    PRIMARY KEY(`id`)
);

CREATE TABLE `account` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `account_number` VARCHAR(50) NOT NULL DEFAULT '',
    `customer_id` BIGINT NOT NULL DEFAULT 0,
    `type` SMALLINT NOT NULL DEFAULT 1, -- 1:savings 2: current
    `bank_id` BIGINT NOT NULL DEFAULT 0,
    `balance` BIGINT NOT NULL DEFAULT 0,
    `interest_rate` DOUBLE NOT NULL DEFAULT 0.0,
    `status` SMALLINT NOT NULL DEFAULT 0,
    `registered_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `delete_flg` SMALLINT NOT NULL DEFAULT 0,
    PRIMARY KEY(`id`)
);

CREATE TABLE `transaction` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `account_number` VARCHAR(50) NOT NULL DEFAULT '',
    `account_from` VARCHAR(50),
    `account_to` VARCHAR(50),
    `type` SMALLINT DEFAULT 0, -- 0: withdraw 1: deposit 2: transfer from 3: transfer to
    currency VARCHAR(50) NOT NULL DEFAULT 'yen',
    `status` SMALLINT DEFAULT 0,
    `amount` BIGINT NOT NULL DEFAULT 0,
--     `deposit` BIGINT NOT NULL DEFAULT 0,
--     `saving` BIGINT NOT NULL DEFAULT 0,
    `transaction_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(`id`)
);

CREATE TABLE `bank` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `branch_id` BIGINT NOT NULL DEFAULT 0,
    `registered_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `delete_flg` SMALLINT NOT NULL DEFAULT 0,
    PRIMARY KEY(`id`)
);

CREATE TABLE `branch` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `registered_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `delete_flg` SMALLINT NOT NULL DEFAULT 0,
    PRIMARY KEY(`id`)
);




# --- !Downs
DROP TABLE `customer`;
DROP TABLE `account`;
DROP TABLE `transaction`;
DROP TABLE `bank`;
DROP TABLE `branch`;
