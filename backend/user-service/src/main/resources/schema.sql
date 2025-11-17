CREATE TABLE IF NOT EXISTS users(
    id uuid PRIMARY KEY,
    email varchar(255) UNIQUE NOT NULL,
    password varchar(255) NOT NULL
);
CREATE TABLE IF NOT EXISTS user_address(
  id uuid PRIMARY KEY,
  user_id uuid NOT NULL REFERENCES users(id),
  firstname varchar(100) NOT NUll,
  lastname varchar(100) NOT NULL,
  patronymic varchar(100) NOT NULL,
  phone varchar(50) NOT NULL,
  city varchar(100) NOT NULL,
  street varchar(100) NOT NULL,
  building varchar(100) NOT NULL,
  postal_code int NOT NULL
);