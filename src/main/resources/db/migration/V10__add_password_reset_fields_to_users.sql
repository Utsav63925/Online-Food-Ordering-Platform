ALTER TABLE foodie_users
ADD COLUMN reset_otp VARCHAR(10),
ADD COLUMN otp_expiry DATETIME;
