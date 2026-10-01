-- Run once on dev/prod before deploying the updated API (ddl-auto=validate).
-- Local ddl-auto=update adds this column on application restart.
-- Existing records remain NULL: their payment status has not been recorded.
ALTER TABLE usersubcription ADD COLUMN payment_status VARCHAR(10) NULL;
