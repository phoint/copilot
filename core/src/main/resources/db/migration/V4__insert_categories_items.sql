-- Insert 10 categories for e-commerce products
INSERT INTO categories (name) VALUES
('Electronics'),
('Books'),
('Clothing'),
('Home & Kitchen'),
('Sports & Outdoors'),
('Health & Personal Care'),
('Toys & Games'),
('Automotive'),
('Beauty'),
('Grocery');
-- Insert subcategories for Electronics
INSERT INTO categories (name, parent_id) VALUES
('Mobile Phones', (SELECT id FROM categories WHERE name = 'Electronics')),
('Laptops', (SELECT id FROM categories WHERE name = 'Electronics')),
('Cameras', (SELECT id FROM categories WHERE name = 'Electronics')),
('Headphones', (SELECT id FROM categories WHERE name = 'Electronics')),
('Televisions', (SELECT id FROM categories WHERE name = 'Electronics'));
-- Insert subcategories for Books
INSERT INTO categories (name, parent_id) VALUES
('Fiction', (SELECT id FROM categories WHERE name = 'Books')),
('Non-Fiction', (SELECT id FROM categories WHERE name = 'Books')),
('Children''s Books', (SELECT id FROM categories WHERE name = 'Books')),
('Educational', (SELECT id FROM categories WHERE name = 'Books')),
('Comics', (SELECT id FROM categories WHERE name = 'Books'));
-- Insert subcategories for Clothing
INSERT INTO categories (name, parent_id) VALUES
('Men''s Clothing', (SELECT id FROM categories WHERE name = 'Clothing')),
('Women''s Clothing', (SELECT id FROM categories WHERE name = 'Clothing')),
('Kids'' Clothing', (SELECT id FROM categories WHERE name = 'Clothing')),
('Shoes', (SELECT id FROM categories WHERE name = 'Clothing')),
('Accessories', (SELECT id FROM categories WHERE name = 'Clothing'));
-- Insert subcategories for Home & Kitchen
INSERT INTO categories (name, parent_id) VALUES
('Furniture', (SELECT id FROM categories WHERE name = 'Home & Kitchen')),
('Appliances', (SELECT id FROM categories WHERE name = 'Home & Kitchen')),
('Cookware', (SELECT id FROM categories WHERE name = 'Home & Kitchen')),
('Bedding', (SELECT id FROM categories WHERE name = 'Home & Kitchen')),
('Decor', (SELECT id FROM categories WHERE name = 'Home & Kitchen'));
-- Insert subcategories for Sports & Outdoors
INSERT INTO categories (name, parent_id) VALUES
('Fitness', (SELECT id FROM categories WHERE name = 'Sports & Outdoors')),
('Outdoor Recreation', (SELECT id FROM categories WHERE name = 'Sports & Outdoors')),
('Team Sports', (SELECT id FROM categories WHERE name = 'Sports & Outdoors')),
('Water Sports', (SELECT id FROM categories WHERE name = 'Sports & Outdoors')),
('Winter Sports', (SELECT id FROM categories WHERE name = 'Sports & Outdoors'));
-- Insert subcategories for Health & Personal Care
INSERT INTO categories (name, parent_id) VALUES
('Vitamins & Supplements', (SELECT id FROM categories WHERE name = 'Health & Personal Care')),
('Personal Care', (SELECT id FROM categories WHERE name = 'Health & Personal Care')),
('Medical Supplies', (SELECT id FROM categories WHERE name = 'Health & Personal Care')),
('Oral Care', (SELECT id FROM categories WHERE name = 'Health & Personal Care')),
('Skin Care', (SELECT id FROM categories WHERE name = 'Health & Personal Care'));
-- Insert subcategories for Toys & Games
INSERT INTO categories (name, parent_id) VALUES
('Action Figures', (SELECT id FROM categories WHERE name = 'Toys & Games')),
('Board Games', (SELECT id FROM categories WHERE name = 'Toys & Games')),
('Dolls', (SELECT id FROM categories WHERE name = 'Toys & Games')),
('Puzzles', (SELECT id FROM categories WHERE name = 'Toys & Games')),
('Outdoor Toys', (SELECT id FROM categories WHERE name = 'Toys & Games'));
-- Insert subcategories for Automotive
INSERT INTO categories (name, parent_id) VALUES
('Car Accessories', (SELECT id FROM categories WHERE name = 'Automotive')),
('Motorcycle Accessories', (SELECT id FROM categories WHERE name = 'Automotive')),
('Tools & Equipment', (SELECT id FROM categories WHERE name = 'Automotive')),
('Replacement Parts', (SELECT id FROM categories WHERE name = 'Automotive')),
('Tires & Wheels', (SELECT id FROM categories WHERE name = 'Automotive'));
-- Insert subcategories for Beauty
INSERT INTO categories (name, parent_id) VALUES
('Makeup', (SELECT id FROM categories WHERE name = 'Beauty')),
('Skincare', (SELECT id FROM categories WHERE name = 'Beauty')),
('Hair Care', (SELECT id FROM categories WHERE name = 'Beauty')),
('Fragrance', (SELECT id FROM categories WHERE name = 'Beauty')),
('Tools & Accessories', (SELECT id FROM categories WHERE name = 'Beauty'));
-- Insert subcategories for Grocery
INSERT INTO categories (name, parent_id) VALUES
('Beverages', (SELECT id FROM categories WHERE name = 'Grocery')),
('Snacks', (SELECT id FROM categories WHERE name = 'Grocery')),
('Pantry Staples', (SELECT id FROM categories WHERE name = 'Grocery')),
('Dairy & Eggs', (SELECT id FROM categories WHERE name = 'Grocery')),
('Frozen Foods', (SELECT id FROM categories WHERE name = 'Grocery'));