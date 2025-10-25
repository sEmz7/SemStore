CREATE TABLE IF NOT EXISTS orders(
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    address_id uuid NOT NULL,
    status varchar(30) NOT NULL,
    created_date timestamp DEFAULT now()
);
CREATE TABLE IF NOT EXISTS order_items(
    id uuid PRIMARY KEY,
    order_id uuid NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    link varchar(50) NOT NULL,
    size varchar(30) NOT NULL,
    configuration varchar(255)
);
