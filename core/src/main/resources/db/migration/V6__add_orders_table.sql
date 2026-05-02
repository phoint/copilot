CREATE TABLE orders (
    id                  INT PRIMARY KEY AUTO_INCREMENT,
    user_id             INT NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    shipping_address    VARCHAR(255),
    promo_code          VARCHAR(50),
    discount_amount     DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    shipping_cost       DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    membership_discount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total_amount        DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
);
