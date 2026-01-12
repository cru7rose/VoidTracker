CREATE TABLE IF NOT EXISTS additional_services (
    id UUID PRIMARY KEY,
    service_code VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    input_type VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS order_required_services (
    order_id UUID NOT NULL,
    service_id UUID NOT NULL,
    PRIMARY KEY (order_id, service_id),
    FOREIGN KEY (order_id) REFERENCES vt_orders(order_id),
    FOREIGN KEY (service_id) REFERENCES additional_services(id)
);
