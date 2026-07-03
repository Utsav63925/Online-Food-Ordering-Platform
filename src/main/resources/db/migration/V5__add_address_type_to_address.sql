-- 1️⃣ Add column as nullable first (SAFE for existing data)
ALTER TABLE address
ADD COLUMN address_type VARCHAR(30);

-- 2️⃣ Mark addresses linked to restaurants as RESTAURANT
UPDATE address a
JOIN restaurant_address ra ON ra.address_id = a.id
SET a.address_type = 'RESTAURANT';

-- 3️⃣ Remaining addresses are USER addresses
UPDATE address
SET address_type = 'USER'
WHERE address_type IS NULL;

-- 4️⃣ Enforce NOT NULL after data is fixed
ALTER TABLE address
MODIFY address_type VARCHAR(30) NOT NULL;

-- 5️⃣ (Optional but recommended) Enum safety
ALTER TABLE address
ADD CONSTRAINT chk_address_type
CHECK (address_type IN ('USER', 'RESTAURANT'));
