INSERT INTO payments (order_id, user_id, amount, status, external_payment_id, created_at, updated_at)
VALUES (100, 200, 50000, 'SUCCESS', 'payment-100', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO payments (order_id, user_id, amount, status, external_payment_id, created_at, updated_at)
VALUES (101, 200, 30000, 'FAILED', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);