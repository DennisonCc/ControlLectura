-- Insertar productos de prueba con stock inicial
INSERT INTO products_stock (product_id, available_stock, reserved_stock, updated_at) VALUES
('a3c2b1d0-6b0e-4f2b-9c1a-2d3f4a5b6c7d', 50, 0, CURRENT_TIMESTAMP),
('b7e8c9d1-2f3a-4b5c-8d9e-1a2b3c4d5e6f', 30, 0, CURRENT_TIMESTAMP),
('c1d2e3f4-5a6b-7c8d-9e0f-1a2b3c4d5e6f', 100, 0, CURRENT_TIMESTAMP),
('d4e5f6a7-8b9c-0d1e-2f3a-4b5c6d7e8f9a', 15, 0, CURRENT_TIMESTAMP),
('e7f8a9b0-1c2d-3e4f-5a6b-7c8d9e0f1a2b', 75, 0, CURRENT_TIMESTAMP)
ON CONFLICT (product_id) DO NOTHING;
