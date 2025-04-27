INSERT INTO payments (order_id, user_id, amount, status, external_payment_id, created_at, updated_at, VERSION)
VALUES (100, 200, 50000, 'SUCCESS', 'payment-100', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO payments (order_id, user_id, amount, status, external_payment_id, created_at, updated_at, VERSION)
VALUES (101, 200, 30000, 'FAILED', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);