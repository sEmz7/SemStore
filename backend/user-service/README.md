# User Authentication Service

Этот микросервис предоставляет API для **регистрации**, **аутентификации** и **управления пользователями** с использованием JWT.

## 🚀 Возможности

- Регистрация новых пользователей
- Авторизация с получением JWT access/refresh токенов
- Обновление токенов по refresh-токену
- Получение информации о пользователе по ID
---
## ⚙️ Технологии

- **Spring Boot** (REST API)
- **Hibernate** (ORM)
- **JWT** (аутентификация и авторизация)
- **OpenAPI/Swagger** (документация API)
- **PostgreSQL** (БД пользователей)

---

## 🔑 Эндпоинты

### 📍 Аутентификация (`/auth`)

#### ▶ Регистрация

`POST /auth/register`

Создание нового пользователя.

**Request body**:

```json
{   
	"email": "example@test.com",
	"password": "password"
}
```

**Response 201**:

```json
{   
	"id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
	"email": "example@test.com" 
}
```

Ошибки:

- `400` — неверные входные данные (некорректный email)
- `409` — пользователь с таким email уже существует

---

#### ▶ Логин

`POST /auth/login`

Авторизация пользователя.

**Request body**:

```json
{
	"email": "example@test.com",
	"password": "password" 
}
```
**Response 200**:

```json
{   
	"token": "jwt access token",
	"refreshToken": "refresh token" 
}
```

Ошибки:

- `400` — ошибка входных данных (некорректный email)
- `401` — неверный пароль
- `404` — пользователь не найден

---

#### ▶ Обновление токена

`POST /auth/refresh`

Обновление JWT access токена.

**Request body**:

```json
{   
	"refreshToken": "refresh token" 
}
```


**Response 200**:

```json
{   
	"token": "new jwt access token",
	"refreshToken": "new refresh token" 
}
```

Ошибки:

- `401` — неверный или просроченный refresh token

---

### 📍 Пользователи (`/users`)

#### ▶ Получение пользователя по ID

`GET /users/{userId}`

**Path parameter**:

- `userId` (UUID) — ID пользователя

**Response 200**:

```json
{   
	"id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
	"email": "example@test.com" 
}
```

---

## 🛠️ Запуск проекта

### 1. Клонирование

`git clone https://github.com/sEmz7/SemStore.git`

### 2. Конфигурация

Создайте .env файл в корне проекта

Пример:
```
POSTGRES_USER=name  
POSTGRES_PASSWORD=password
POSTGRES_DB=user-service-db
JWT_SECRET=C9zdfM/grQ1Rm7px1XG3EU64Ud9/UDG7V1tGF2euCvsK03o/KOIx/KTW97nYAGn1  
  
SPRING_DATASOURCE_URL=jdbc:postgresql://user-service-db:5432/user-service-db
```

### 3. Сборка и запуск

`docker compose up -d --build`

Сервис будет доступен по адресу:  
👉 `http://localhost:8080`

---

## 📖 Документация API

Swagger UI доступен по адресу:  
👉 `http://localhost:8080/swagger-ui.html`

---

## 📬 Ответы об ошибках

Все ошибки возвращаются в формате:

```json
{   
	"message": "error message" 
}
```