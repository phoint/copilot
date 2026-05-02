-- ============================================
-- Test Data for Order Service Testing
-- ============================================

-- ============================================
-- INSERT TEST USERS
-- ============================================
INSERT INTO users (username, email, password, role, status, created_at, updated_at) VALUES
('alice_customer', 'alice@example.com', '$2a$10$VIHEMdCYwPTb0FS7M6YRJetBGCbkPBH8eFMoXNO/Br4ylAP8KV5uu', 'USER', 'ACTIVE', NOW(), NOW()),
('bob_customer', 'bob@example.com', '$2a$10$VIHEMdCYwPTb0FS7M6YRJetBGCbkPBH8eFMoXNO/Br4ylAP8KV5uu', 'USER', 'ACTIVE', NOW(), NOW()),
('charlie_customer', 'charlie@example.com', '$2a$10$VIHEMdCYwPTb0FS7M6YRJetBGCbkPBH8eFMoXNO/Br4ylAP8KV5uu', 'USER', 'ACTIVE', NOW(), NOW()),
('diana_customer', 'diana@example.com', '$2a$10$VIHEMdCYwPTb0FS7M6YRJetBGCbkPBH8eFMoXNO/Br4ylAP8KV5uu', 'USER', 'ACTIVE', NOW(), NOW());

-- ============================================
-- INSERT TEST CATEGORIES (if not exists from V4)
-- ============================================
-- Categories are already seeded in V4, so we can reference them
-- But adding some for completeness

-- ============================================
-- INSERT TEST PRODUCTS (additional test products)
-- ============================================
-- Products are already seeded in V5, reference product IDs 1-141

-- ============================================
-- INSERT TEST ORDERS - PENDING STATE
-- ============================================
INSERT INTO orders (user_id, status, shipping_address, promo_code, discount_amount, shipping_cost, membership_discount, total_amount, created_at, updated_at)
VALUES
(1, 'PENDING', '123 Main Street, Springfield, IL', NULL, 0.00, 0.00, 0.00, 1999.98, NOW(), NOW()),
(2, 'PENDING', '456 Oak Avenue, Chicago, IL', 'SAVE10', 200.00, 0.00, 0.00, 1799.98, NOW(), NOW());

-- INSERT ORDER ITEMS for PENDING orders
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(1, 1, 2, 999.99),  -- iPhone 13 x2
(2, 2, 1, 1999.99); -- iPhone 14 x1

-- ============================================
-- INSERT TEST ORDERS - CONFIRMED STATE
-- ============================================
INSERT INTO orders (user_id, status, shipping_address, promo_code, discount_amount, shipping_cost, membership_discount, total_amount, created_at, updated_at)
VALUES
(1, 'CONFIRMED', '123 Main Street, Springfield, IL', NULL, 0.00, 15.00, 0.00, 2014.98, NOW(), NOW()),
(3, 'CONFIRMED', '789 Pine Road, Naperville, IL', NULL, 0.00, 0.00, 0.00, 3249.96, NOW(), NOW());

-- INSERT ORDER ITEMS for CONFIRMED orders
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(3, 3, 1, 1299.99), -- Samsung Galaxy S23
(3, 15, 1, 1949.97), -- Laptop
(4, 4, 2, 1624.98), -- Google Pixel 8 x2
(4, 5, 1, 499.99);  -- iPad Pro

-- ============================================
-- INSERT TEST ORDERS - PROCESSING STATE
-- ============================================
INSERT INTO orders (user_id, status, shipping_address, promo_code, discount_amount, shipping_cost, membership_discount, total_amount, created_at, updated_at)
VALUES
(2, 'PROCESSING', '456 Oak Avenue, Chicago, IL', NULL, 0.00, 20.00, 0.00, 2519.96, NOW(), NOW()),
(4, 'PROCESSING', '321 Elm Street, Evanston, IL', 'SUMMER20', 300.00, 15.00, 0.00, 2614.95, NOW(), NOW());

-- INSERT ORDER ITEMS for PROCESSING orders
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(5, 6, 1, 799.99),   -- MacBook Air
(5, 16, 1, 1699.97), -- Desktop PC
(6, 7, 2, 899.98),   -- AirPods Pro x2
(6, 17, 1, 2299.97); -- Gaming Laptop

-- ============================================
-- INSERT TEST ORDERS - SHIPPED STATE
-- ============================================
INSERT INTO orders (user_id, status, shipping_address, promo_code, discount_amount, shipping_cost, membership_discount, total_amount, created_at, updated_at)
VALUES
(1, 'SHIPPED', '123 Main Street, Springfield, IL', NULL, 0.00, 25.00, 50.00, 4574.95, NOW(), NOW()),
(3, 'SHIPPED', '789 Pine Road, Naperville, IL', NULL, 100.00, 0.00, 0.00, 2849.96, NOW(), NOW());

-- INSERT ORDER ITEMS for SHIPPED orders
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(7, 8, 3, 599.97),   -- Sony WH-1000XM5 x3
(7, 18, 1, 3999.98), -- 4K TV
(8, 9, 2, 449.98),   -- USB-C Cable x2
(8, 19, 1, 2499.98); -- Curved Monitor

-- ============================================
-- INSERT TEST ORDERS - DELIVERED STATE
-- ============================================
INSERT INTO orders (user_id, status, shipping_address, promo_code, discount_amount, shipping_cost, membership_discount, total_amount, created_at, updated_at)
VALUES
(2, 'DELIVERED', '456 Oak Avenue, Chicago, IL', NULL, 0.00, 0.00, 0.00, 2999.97, NOW(), NOW()),
(4, 'DELIVERED', '321 Elm Street, Evanston, IL', NULL, 0.00, 15.00, 100.00, 3549.94, NOW(), NOW());

-- INSERT ORDER ITEMS for DELIVERED orders
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(9, 10, 1, 2999.97),  -- iPad Air
(10, 11, 1, 2999.99), -- Galaxy Tab
(10, 20, 1, 699.95);  -- Mechanical Keyboard

-- ============================================
-- INSERT TEST ORDERS - CANCELLED STATE
-- ============================================
INSERT INTO orders (user_id, status, shipping_address, promo_code, discount_amount, shipping_cost, membership_discount, total_amount, created_at, updated_at)
VALUES
(1, 'CANCELLED', '123 Main Street, Springfield, IL', NULL, 0.00, 0.00, 0.00, 0.00, NOW(), NOW()),
(3, 'CANCELLED', '789 Pine Road, Naperville, IL', NULL, 0.00, 0.00, 0.00, 0.00, NOW(), NOW());

-- INSERT ORDER ITEMS for CANCELLED orders
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(11, 12, 1, 599.99),  -- Smart Watch
(12, 13, 2, 199.98);  -- Phone Stand x2

-- ============================================
-- INSERT TEST ORDERS - REFUNDED STATE
-- ============================================
INSERT INTO orders (user_id, status, shipping_address, promo_code, discount_amount, shipping_cost, membership_discount, total_amount, created_at, updated_at)
VALUES
(2, 'REFUNDED', '456 Oak Avenue, Chicago, IL', NULL, 0.00, 0.00, 0.00, 0.00, NOW(), NOW());

-- INSERT ORDER ITEMS for REFUNDED orders
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(13, 14, 1, 699.99); -- Wireless Charger

-- ============================================
-- DATA SUMMARY
-- ============================================
-- Users: 4 test customers (alice, bob, charlie, diana)
-- Orders: 12 test orders across all 7 states
--   - 2 PENDING orders
--   - 2 CONFIRMED orders
--   - 2 PROCESSING orders
--   - 2 SHIPPED orders
--   - 2 DELIVERED orders
--   - 1 CANCELLED order (and 1 more for 2 total)
--   - 1 REFUNDED order
-- Order Items: 28 items across all orders
-- Products used: IDs 1-20 (from existing V5 seed data)
-- ============================================
