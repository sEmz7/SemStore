INSERT INTO orders (id, name, user_id, address_id, status, created_date, tracking_number)
VALUES ('08acd0a9-b1f8-40ab-a69a-efec2ab7fda6',
        'name',
        '22222222-2222-2222-2222-222222222222',
        '33333333-3333-3333-3333-333333333333',
        'CREATED',
        NOW(),
        'ORD-919810D6927E');

INSERT INTO order_items (id, order_id, link, size, configuration)
VALUES ('99999999-9999-9999-9999-999999999999',
        '08acd0a9-b1f8-40ab-a69a-efec2ab7fda6',
        'link',
        '42',
        'green');