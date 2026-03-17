INSERT INTO users (
    id,
    email,
    password,
    role,
    email_verified
) VALUES (
             '11111111-1111-1111-1111-111111111111',
             'test@mail.com',
             '$2a$10$abcdefghijklmnopqrstuv',
             'ROLE_USER',
             false
         );

INSERT INTO verification_code (
    id,
    user_id,
    code_hash,
    expires_at,
    attempts,
    created_at
) VALUES (
             '22222222-2222-2222-2222-222222222222',
             '11111111-1111-1111-1111-111111111111',
             '$2a$10$oldoldoldoldoldoldold',
             DATEADD('MINUTE', 10, CURRENT_TIMESTAMP),
             0,
             DATEADD('MINUTE', -2, CURRENT_TIMESTAMP)
         );