INSERT INTO orders (id, user_id, address_id, status, created_date)
VALUES ('08acd0a9-b1f8-40ab-a69a-efec2ab7fda6',
        '22222222-2222-2222-2222-222222222222',
        '11111111-1111-1111-1111-111111111111',
        'CREATED',
        now());

INSERT INTO order_items (id, order_id, link, size, configuration)
VALUES ('99999999-9999-9999-9999-999999999999',
        '08acd0a9-b1f8-40ab-a69a-efec2ab7fda6',
        'link',
        '42',
        'green');