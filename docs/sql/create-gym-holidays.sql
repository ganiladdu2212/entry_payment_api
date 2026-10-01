-- Run once before deploying with the dev or prod profile (ddl-auto=validate).
CREATE TABLE gym_holidays (
    holiday_id BIGINT NOT NULL AUTO_INCREMENT,
    cust_id BIGINT NOT NULL,
    holiday_date DATE NOT NULL,
    purpose VARCHAR(250) NOT NULL,
    created_date DATETIME(6) NOT NULL,
    PRIMARY KEY (holiday_id),
    CONSTRAINT uk_gym_holiday_customer_date UNIQUE (cust_id, holiday_date),
    CONSTRAINT fk_gym_holiday_customer FOREIGN KEY (cust_id) REFERENCES customers(cust_id)
);
