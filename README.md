# Football Pitch Marketplace Minsk

> Лабораторная работа по JPA (Hibernate/Spring Data)

[![Java 17](https://img.shields.io/badge/Java-17-orange)](https://adoptium.net/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.0%2B-brightgreen)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9%2B-blue)](https://maven.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-blue)](https://www.postgresql.org/)

## Что реализовано по условиям

1. Подключена реляционная БД: `PostgreSQL` + `Spring Data JPA` + `Hibernate`.
2. Реализовано 5 сущностей:
- `User`
- `Pitch`
- `Booking`
- `OpenGame`
- `EquipmentOffer`
3. Реализован CRUD API для всех 5 сущностей.
4. Настроены и обоснованы `CascadeType` и `FetchType`.
5. Продемонстрирована проблема `N+1` и решение через `@EntityGraph`.
6. Продемонстрировано частичное сохранение без `@Transactional` и полный rollback с `@Transactional`.
7. Нарисована ER-диаграмма с PK/FK и связями: [docs/ER_DIAGRAM.md](docs/ER_DIAGRAM.md).

Дополнительно: во всех связях используется `FetchType.LAZY`.

## Модель и связи

- `Pitch 1 -> N Booking`
- `Pitch 1 -> N EquipmentOffer`
- `User 1 -> N Booking` (организатор)
- `Booking 1 -> 0..1 OpenGame`
- `User 1 -> N OpenGame` (организатор)
- `User N <-> N OpenGame` (participants через `open_game_participants`)

## FetchType и CascadeType (обоснование)

- `FetchType.LAZY` задан явно во всех ассоциациях, чтобы не загружать связные графы заранее и контролировать SQL.
- Каскад применен только для жизненно зависимой связи:
  - `Pitch -> EquipmentOffer`: `PERSIST`, `MERGE`, `REMOVE`, `orphanRemoval=true`.
- Для `ManyToMany` (`OpenGame.participants`) каскад удаления не используется, чтобы удаление игры не удаляло пользователей.
- Для `Booking`/`OpenGame`/`User` каскады не включены, так как это отдельные агрегаты.

## N+1 демонстрация

- Плохой сценарий: `GET /api/v1/demos/n-plus-one/bad`
  - Загружает `OpenGame` списком и затем обращается к `participants` в цикле.
- Исправленный сценарий: `GET /api/v1/demos/n-plus-one/solved`
  - Использует `@EntityGraph(attributePaths = "participants")`.

Оба endpoint возвращают `executedStatements` (через `Hibernate Statistics`) для сравнения количества SQL.

## Транзакции: partial save vs rollback

- Без транзакции: `POST /api/v1/demos/transactions/without-transaction`
  - Сохраняет несколько связанных сущностей, затем выбрасывает исключение.
  - Уже сохраненные записи остаются в БД (partial save).
- С транзакцией: `POST /api/v1/demos/transactions/with-transaction`
  - Та же логика, но метод помечен `@Transactional`.
  - При исключении операция откатывается полностью.

Endpoint возвращает `before`/`after` со счетчиками сущностей.

## CRUD API

### Pitches
- `GET /api/v1/pitches?district=...`
- `GET /api/v1/pitches/{id}`
- `POST /api/v1/pitches`
- `PUT /api/v1/pitches/{id}`
- `DELETE /api/v1/pitches/{id}`

### Users
- `GET /api/v1/users`
- `GET /api/v1/users/{id}`
- `POST /api/v1/users`
- `PUT /api/v1/users/{id}`
- `DELETE /api/v1/users/{id}`

### Bookings
- `GET /api/v1/bookings`
- `GET /api/v1/bookings/{id}`
- `POST /api/v1/bookings`
- `PUT /api/v1/bookings/{id}`
- `DELETE /api/v1/bookings/{id}`

### Open games
- `GET /api/v1/open-games`
- `GET /api/v1/open-games/{id}`
- `POST /api/v1/open-games`
- `PUT /api/v1/open-games/{id}`
- `DELETE /api/v1/open-games/{id}`

### Equipment offers
- `GET /api/v1/equipment-offers`
- `GET /api/v1/equipment-offers/{id}`
- `POST /api/v1/equipment-offers`
- `PUT /api/v1/equipment-offers/{id}`
- `DELETE /api/v1/equipment-offers/{id}`

## Запуск

Перед запуском приложения создай БД в PostgreSQL:

```sql
CREATE DATABASE pitchmarket;
CREATE DATABASE pitchmarket_test;
```

По умолчанию используются:
- `DB_URL=jdbc:postgresql://localhost:5432/pitchmarket`
- `DB_USERNAME=postgres`
- `DB_PASSWORD=postgres`

Если у тебя другие параметры, задай их через переменные окружения.

```bash
./mvnw spring-boot:run
```

Приложение запускается на `http://localhost:8080`.

Для демонстрации преподавателю можно открыть `pgAdmin` и показать таблицы:
- `users`
- `pitches`
- `bookings`
- `open_games`
- `equipment_offers`
- `open_game_participants`

## Тесты

```bash
./mvnw test
```

В тестах используется отдельный PostgreSQL-конфиг (`src/test/resources/application.properties`) и отдельная БД `pitchmarket_test`.
Если логин/пароль отличаются от дефолтных, запускай так:

```bash
TEST_DB_URL=jdbc:postgresql://localhost:5432/pitchmarket_test \
TEST_DB_USERNAME=postgres \
TEST_DB_PASSWORD=<your_password> \
./mvnw test
```

Интеграционный тест `JpaRequirementsIntegrationTest` проверяет:
- CRUD,
- N+1 bad/solved,
- частичное сохранение без транзакции,
- rollback с транзакцией.

## SonarCloud

- Проект: https://sonarcloud.io/summary/new_code?id=SAKU102007_sem_4_java&branch=main
