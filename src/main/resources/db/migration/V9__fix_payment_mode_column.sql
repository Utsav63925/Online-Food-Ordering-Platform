-- Step 1: Drop existing column
ALTER TABLE food_orders
DROP COLUMN payment_mode;

-- Step 2: Recreate as ENUM (string-based)
ALTER TABLE food_orders
ADD COLUMN payment_mode ENUM (
    'CASH_ON_DELIVERY',
    'CREDIT_CARD',
    'DEBIT_CARD',
    'UPI'
) AFTER payment_id;
