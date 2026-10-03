INSERT INTO customers (id, full_name, email, phone_number, national_id, created_at)
VALUES
    (1, 'Alice Ndzi', 'alice@m2ibank.com', '+237600000001', 'CNI000001', CURRENT_TIMESTAMP),
    (2, 'Brice Tchoumi', 'brice@m2ibank.com', '+237600000002', 'CNI000002', CURRENT_TIMESTAMP),
    (3, 'Clarisse Mvondo', 'clarisse@m2ibank.com', '+237600000003', 'CNI000003', CURRENT_TIMESTAMP)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO accounts (id, account_number, balance, account_type, customer_id, created_at)
VALUES
    (1, 'DB-ACC-0001', 150000.00, 'CURRENT', 1, CURRENT_TIMESTAMP),
    (2, 'DB-ACC-0002', 250000.00, 'SAVINGS', 2, CURRENT_TIMESTAMP),
    (3, 'DB-ACC-0003', 50000.00, 'CURRENT', 3, CURRENT_TIMESTAMP)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO transfers (id, source_account_id, destination_account_id, amount, description, created_at)
VALUES
    (1, 1, 2, 10000.00, 'Initial transfer for testing', CURRENT_TIMESTAMP),
    (2, 2, 3, 15000.00, 'Savings to current transfer', CURRENT_TIMESTAMP)
    ON CONFLICT (id) DO NOTHING;

SELECT setval('customers_id_seq', (SELECT MAX(id) FROM customers));
SELECT setval('accounts_id_seq', (SELECT MAX(id) FROM accounts));
SELECT setval('transfers_id_seq', (SELECT MAX(id) FROM transfers));
