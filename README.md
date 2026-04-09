# Samykin Pay

Монорепозиторий дипломного проекта по теме:

`Разработка программного обеспечения для распределения и выдачи денежных выплат в ИП Самыкин`

## Структура

- `product-api` — Spring Boot 3 + Gradle + PostgreSQL + JWT + Flyway + Spring AI + GigaChat
- `product-web` — React + Vite + React Router + Playwright

## Роли

- Администратор
- Бухгалтер
- Сотрудник

## Локальный запуск

### API

Из корня репозитория:

```bash
docker compose up -d --build
```

Документация Swagger:

`http://localhost:8080/swagger-ui`

### Web

```bash
cd product-web
npm install
npm run dev
```

Web URL:

`http://localhost:5173`

## Тестовые пользователи

Файл:

`product-api/users.txt`

## E2E

```bash
cd product-web
npx playwright test
```

Скриншоты сохраняются в:

`product-web/artifacts/screenshots/`
