-- 1️⃣ Add normalized_name column
ALTER TABLE food_items
ADD COLUMN normalized_name VARCHAR(255);

-- 2️⃣ Backfill normalized_name for existing rows
-- Normalize = lowercase + remove all spaces
UPDATE food_items
SET normalized_name = LOWER(REPLACE(name, ' ', ''));

-- 3️⃣ Make column NOT NULL
ALTER TABLE food_items
MODIFY normalized_name VARCHAR(255) NOT NULL;

-- 4️⃣ Add UNIQUE constraint on normalized_name
CREATE UNIQUE INDEX uk_food_items_normalized_name
ON food_items (normalized_name);
