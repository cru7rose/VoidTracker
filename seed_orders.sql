-- ============================================
-- CORRECTED SEED DATA WITH PROPERTIES JSONB
-- ============================================

-- 1. Addresses (Warsaw landmarks)
INSERT INTO vt_addresses (address_id, street, city, postal_code, country, latitude, longitude, tenant_id, created_at, updated_at, version)
VALUES 
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Plac Defilad 1', 'Warszawa', '00-901', 'PL', 52.231958, 21.006725, '11111111-1111-1111-1111-111111111111', now(), now(), 1),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'Plac Zamkowy 4', 'Warszawa', '00-277', 'PL', 52.248037, 21.015243, '11111111-1111-1111-1111-111111111111', now(), now(), 1),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 'Grzybowska 79', 'Warszawa', '00-844', 'PL', 52.232378, 20.980860, '11111111-1111-1111-1111-111111111111', now(), now(), 1),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 'Agrykola 1', 'Warszawa', '00-460', 'PL', 52.215286, 21.035319, '11111111-1111-1111-1111-111111111111', now(), now(), 1),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a05', 'Stanislawa Kostki Potockiego 10/16', 'Warszawa', '02-958', 'PL', 52.165181, 21.090547, '11111111-1111-1111-1111-111111111111', now(), now(), 1)
ON CONFLICT (address_id) DO NOTHING;

-- 2. Customer
INSERT INTO vt_customers (customer_id, tenant_id, customer_number, customer_name, customer_type, created_at, updated_at, version)
VALUES ('c1111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', 'CUST-001', 'Test Customer Sp. z o.o.', 'B2B', now(), now(), 1)
ON CONFLICT (customer_id) DO NOTHING;

-- 3. Orders with FULL EAV Properties (CRITICAL FIX)
INSERT INTO vt_orders (
    order_id, 
    client_id, 
    ordering_customer_id, 
    billing_customer_id, 
    delivery_address_id, 
    status, 
    total_weight_kg, 
    total_volume_m3, 
    tenant_id, 
    created_at, 
    updated_at, 
    version, 
    requested_delivery_date, 
    requires_signature, 
    stackable,
    properties  -- 🔴 CRITICAL: Must include coordinates for map rendering
) VALUES 
(
    'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380001',
    '706a86f7-e44d-4fcf-b8e0-ebdee3492bd8',
    'c1111111-1111-1111-1111-111111111111',
    'c1111111-1111-1111-1111-111111111111',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01',
    'NEW',
    15.5,
    0.05,
    '11111111-1111-1111-1111-111111111111',
    now(),
    now(),
    1,
    CURRENT_DATE + INTERVAL '1 day',
    true,
    true,
    '{"deliveryAddress": {"lat": 52.231958, "lon": 21.006725, "street": "Plac Defilad 1", "streetNumber": "1", "city": "Warszawa", "postalCode": "00-901", "country": "PL"}, "pickupAddress": {"lat": 52.2297, "lon": 21.0122, "street": "Świętokrzyska", "streetNumber": "12", "city": "Warszawa", "postalCode": "00-050", "country": "PL"}, "packageDetails": {"weight": 15.5, "volume": 0.05, "colli": 1, "barcode1": "PKG001"}}'::jsonb
),
(
    'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380002',
    '706a86f7-e44d-4fcf-b8e0-ebdee3492bd8',
    'c1111111-1111-1111-1111-111111111111',
    'c1111111-1111-1111-1111-111111111111',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02',
    'NEW',
    8.2,
    0.02,
    '11111111-1111-1111-1111-111111111111',
    now(),
    now(),
    1,
    CURRENT_DATE + INTERVAL '1 day',
    true,
    true,
    '{"deliveryAddress": {"lat": 52.248037, "lon": 21.015243, "street": "Plac Zamkowy", "streetNumber": "4", "city": "Warszawa", "postalCode": "00-277", "country": "PL"}, "pickupAddress": {"lat": 52.2297, "lon": 21.0122, "street": "Świętokrzyska", "streetNumber": "12", "city": "Warszawa", "postalCode": "00-050", "country": "PL"}, "packageDetails": {"weight": 8.2, "volume": 0.02, "colli": 1, "barcode1": "PKG002"}}'::jsonb
),
(
    'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380003',
    '706a86f7-e44d-4fcf-b8e0-ebdee3492bd8',
    'c1111111-1111-1111-1111-111111111111',
    'c1111111-1111-1111-1111-111111111111',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03',
    'NEW',
    25.0,
    0.12,
    '11111111-1111-1111-1111-111111111111',
    now(),
    now(),
    1,
    CURRENT_DATE + INTERVAL '1 day',
    true,
    false,
    '{"deliveryAddress": {"lat": 52.232378, "lon": 20.980860, "street": "Grzybowska", "streetNumber": "79", "city": "Warszawa", "postalCode": "00-844", "country": "PL"}, "pickupAddress": {"lat": 52.2297, "lon": 21.0122, "street": "Świętokrzyska", "streetNumber": "12", "city": "Warszawa", "postalCode": "00-050", "country": "PL"}, "packageDetails": {"weight": 25.0, "volume": 0.12, "colli": 2, "barcode1": "PKG003"}}'::jsonb
),
(
    'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380004',
    '706a86f7-e44d-4fcf-b8e0-ebdee3492bd8',
    'c1111111-1111-1111-1111-111111111111',
    'c1111111-1111-1111-1111-111111111111',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a04',
    'NEW',
    5.0,
    0.01,
    '11111111-1111-1111-1111-111111111111',
    now(),
    now(),
    1,
    CURRENT_DATE + INTERVAL '1 day',
    true,
    true,
    '{"deliveryAddress": {"lat": 52.215286, "lon": 21.035319, "street": "Agrykola", "streetNumber": "1", "city": "Warszawa", "postalCode": "00-460", "country": "PL"}, "pickupAddress": {"lat": 52.2297, "lon": 21.0122, "street": "Świętokrzyska", "streetNumber": "12", "city": "Warszawa", "postalCode": "00-050", "country": "PL"}, "packageDetails": {"weight": 5.0, "volume": 0.01, "colli": 1, "barcode1": "PKG004"}}'::jsonb
),
(
    'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380005',
    '706a86f7-e44d-4fcf-b8e0-ebdee3492bd8',
    'c1111111-1111-1111-1111-111111111111',
    'c1111111-1111-1111-1111-111111111111',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a05',
    'NEW',
    42.1,
    0.35,
    '11111111-1111-1111-1111-111111111111',
    now(),
    now(),
    1,
    CURRENT_DATE + INTERVAL '1 day',
    true,
    false,
    '{"deliveryAddress": {"lat": 52.165181, "lon": 21.090547, "street": "Stanislawa Kostki Potockiego", "streetNumber": "10/16", "city": "Warszawa", "postalCode": "02-958", "country": "PL"}, "pickupAddress": {"lat": 52.2297, "lon": 21.0122, "street": "Świętokrzyska", "streetNumber": "12", "city": "Warszawa", "postalCode": "00-050", "country": "PL"}, "packageDetails": {"weight": 42.1, "volume": 0.35, "colli": 3, "barcode1": "PKG005"}}'::jsonb
)
ON CONFLICT (order_id) DO NOTHING;
