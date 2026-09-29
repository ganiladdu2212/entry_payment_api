-- Apply after add-payment-status.sql on dev/prod before deploying.
ALTER TABLE usersubcription MODIFY COLUMN payment_status VARCHAR(20) NULL;
