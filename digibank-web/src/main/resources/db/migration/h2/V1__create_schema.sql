CREATE TABLE IF NOT EXISTS customers (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         full_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone_number VARCHAR(30) NOT NULL UNIQUE,
    national_id VARCHAR(30) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS accounts (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        account_number VARCHAR(30) NOT NULL UNIQUE,
    balance NUMERIC(19,2) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    customer_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_accounts_customer
    FOREIGN KEY (customer_id)
    REFERENCES customers(id)
    ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS transfers (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         source_account_id BIGINT NOT NULL,
                                         destination_account_id BIGINT NOT NULL,
                                         amount NUMERIC(19,2) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transfer_source_account
    FOREIGN KEY (source_account_id)
    REFERENCES accounts(id)
    ON DELETE CASCADE,
    CONSTRAINT fk_transfer_destination_account
    FOREIGN KEY (destination_account_id)
    REFERENCES accounts(id)
    ON DELETE CASCADE
    );
