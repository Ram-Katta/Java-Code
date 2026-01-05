-- seed.sql
-- Dummy data for testing

INSERT INTO users (username, email) VALUES
('alice', 'alice@example.com'),
('bob', 'bob@example.com'),
('charlie', 'charlie@example.com');

INSERT INTO products (name, price, stock) VALUES
('Laptop', 999.99, 10),
('Keyboard', 49.99, 50),
('Mouse', 29.99, 100),
('Monitor', 199.99, 20);

INSERT INTO orders (user_id, total) VALUES
(1, 1049.98),
(2, 229.98);

INSERT INTO order_items (order_id, product_id, quantity, price) VALUES
(1, 1, 1, 999.99),
(1, 2, 1, 49.99),
(2, 4, 1, 199.99),
(2, 3, 1, 29.99);