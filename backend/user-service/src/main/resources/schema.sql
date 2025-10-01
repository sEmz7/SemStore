CREATE TABLE IF NOT EXISTS users(
    id uuid PRIMARY KEY,
    email varchar(255) UNIQUE NOT NULL,
    password varchar(255) NOT NULL
);