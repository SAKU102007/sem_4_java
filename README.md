# Football Pitch Marketplace Minsk

> Лабораторная работа №1 по Java/Spring Boot (БГУИР)

[![Java 17](https://img.shields.io/badge/Java-17-orange)](https://adoptium.net/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.0%2B-brightgreen)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9%2B-blue)](https://maven.apache.org/)
[![H2](https://img.shields.io/badge/Database-H2-lightgrey)](https://www.h2database.com/html/main.html)

REST-сервис для поиска футбольных полей в Минске с фильтрацией, сортировкой, проверкой доступности по времени и опциональным учетом инвентаря.

Основная реализация лабораторной работы находится в каталоге `lab_1`.

## О проекте

В проекте реализованы требования лабораторной работы:
- `GET` endpoint с `@RequestParam`: расширенный поиск полей
- `GET` endpoint с `@PathVariable`: получение поля по `id`
- Слоистая архитектура: `Controller -> Service -> Repository`
- DTO + Mapper для API-ответов
- Централизованная обработка ошибок (`ApiErrorResponse`)
- Seed-данные в H2 для быстрого запуска и демонстрации

## Стек технологий

- Java 17
- Spring Boot 3
- Spring Data JPA
- Maven
- H2 (in-memory)
- JUnit 5 + MockMvc (интеграционные тесты)

## Структура проекта

```text
lab_1/src/main/java/by/bsuir/pitchmarketplace
├── controller/      # REST API
├── service/         # бизнес-логика
├── repository/      # доступ к данным
├── domain/entity/   # JPA-сущности
├── domain/enums/    # перечисления
├── dto/             # запросы/ответы API
├── mapper/          # преобразование entity -> DTO
└── exception/       # обработка ошибок

lab_1/src/main/resources
├── application.properties
└── data.sql

lab_1/src/test/java/by/bsuir/pitchmarketplace
└── PitchControllerIntegrationTest.java
```

## Запуск проекта

```bash
./mvnw -pl lab_1 spring-boot:run
```

Приложение стартует на `http://localhost:8080`.

## API

### 1) Поиск полей

`GET /api/v1/pitches/search`

Поддерживаемые параметры:
- `pitchType` = `FIVE_TURF | FIVE_FUTSAL | EIGHT | ELEVEN`
- `priceFrom`, `priceTo`
- `durationMinutes` (60..180, шаг 30)
- `skillMin`, `skillMax` (1..100)
- `district`, `metro`
- `lat`, `lng`, `radiusKm`
- `desiredStartAt`, `desiredEndAt` (строгая доступность на весь интервал)
- `sort` = `price_asc | price_desc | avg_skill_asc | avg_skill_desc | distance_to_me_asc | distance_to_me_desc`
- `needInventory` (`false` по умолчанию)
- `ballQty`, `bibsQty`
- заголовок `X-User-Id` (обязателен только для `distance_to_me_*`)

Пример запроса:

```bash
curl "http://localhost:8080/api/v1/pitches/search?desiredStartAt=2026-03-01T19:00:00&desiredEndAt=2026-03-01T21:00:00&sort=price_asc"
```

### 2) Получение поля по id

`GET /api/v1/pitches/{id}`

Пример:

```bash
curl "http://localhost:8080/api/v1/pitches/1"
```

## Примеры демонстрации для отчета

- Запуск приложения (`Started PitchMarketplaceApplication`)
- Успешный поиск с фильтрами
- Сортировка по цене
- Поиск по временному интервалу
- Поиск с `needInventory=true`
- Получение поля по `id`
- Ошибка `400 Bad Request` без `X-User-Id` при `distance_to_me_asc`

## Тестирование

Запуск тестов:

```bash
./mvnw -pl lab_1 test
```

В проекте есть интеграционные тесты `PitchControllerIntegrationTest`, которые проверяют:
- фильтрацию,
- сортировки,
- временную доступность,
- опциональный инвентарь,
- обработку ошибок,
- получение поля по `id`.

## Автор

Студенческий проект для лабораторной работы.
