-- Run on dev/prod before deploying. Local profile creates these via ddl-auto=update.
CREATE TABLE user_attendance_credentials (
  credential_id BIGINT NOT NULL AUTO_INCREMENT,
  cust_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  mobile_number VARCHAR(20) NOT NULL,
  pin_hash VARCHAR(100) NOT NULL,
  device_unique_id VARCHAR(100) NOT NULL,
  created_date DATETIME(6) NOT NULL,
  PRIMARY KEY (credential_id),
  CONSTRAINT uk_attendance_credential_user UNIQUE (user_id),
  CONSTRAINT uk_attendance_credential_mobile UNIQUE (cust_id, mobile_number),
  CONSTRAINT uk_attendance_credential_device UNIQUE (cust_id, device_unique_id),
  CONSTRAINT fk_attendance_credential_customer FOREIGN KEY (cust_id) REFERENCES customers (cust_id),
  CONSTRAINT fk_attendance_credential_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE TABLE user_attendance_events (
  attendance_event_id BIGINT NOT NULL AUTO_INCREMENT,
  cust_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  user_name VARCHAR(150) NOT NULL,
  mobile_number VARCHAR(20) NOT NULL,
  device_unique_id VARCHAR(100) NOT NULL,
  action_type VARCHAR(10) NOT NULL,
  created_date DATETIME(6) NOT NULL,
  PRIMARY KEY (attendance_event_id),
  INDEX idx_attendance_user_date (user_id, created_date),
  INDEX idx_attendance_customer_date (cust_id, created_date),
  CONSTRAINT fk_attendance_event_customer FOREIGN KEY (cust_id) REFERENCES customers (cust_id),
  CONSTRAINT fk_attendance_event_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);
