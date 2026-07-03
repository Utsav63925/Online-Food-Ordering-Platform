CREATE TABLE food_rating (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    food_id VARCHAR(255) NOT NULL,
    rating DOUBLE NOT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    CONSTRAINT uq_user_food UNIQUE (user_id, food_id),
    CONSTRAINT fk_food_rating_user FOREIGN KEY (user_id) REFERENCES foodie_users(id),
    CONSTRAINT fk_food_rating_food FOREIGN KEY (food_id) REFERENCES food_items(id)
);
