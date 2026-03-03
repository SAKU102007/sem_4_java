# Football Pitch Marketplace Minsk

> Лабораторная работа №1 по Java/Spring Boot (БГУИР)

REST-сервис для работы с одной ключевой сущностью предметной области: футбольное поле (`Pitch`).

## Соответствие требованиям ЛР1

- Spring Boot приложение на Java 17
- REST API для одной сущности `Pitch`
- `GET` endpoint с `@RequestParam`: фильтрация по району
- `GET` endpoint с `@PathVariable`: получение поля по `id`
- Слои: `Controller -> Service -> Repository`
- DTO + Mapper между `Pitch` и API-ответом
- Настроен Checkstyle

## Стек

- Java 17
- Spring Boot 3
- Maven
- JUnit 5 + MockMvc

## Структура проекта

```text
src/main/java/pitchmarketplace
├── controller/      # REST API
├── service/         # бизнес-логика
├── repository/      # in-memory хранилище
├── domain/entity/   # сущность Pitch
├── domain/enums/    # перечисления
├── dto/             # API-ответы
├── mapper/          # entity -> DTO
├── SeedData.java    # начальные данные
└── PitchMarketplaceApplication.java

src/test/java/pitchmarketplace
└── PitchControllerIntegrationTest.java
```

## Запуск

```bash
./mvnw spring-boot:run
```

Приложение запускается на `http://localhost:8080`.

## API

### Получить список полей (опционально с фильтром по району)

`GET /api/v1/pitches`

Параметр:
- `district` (необязательный)

Примеры:

```bash
curl "http://localhost:8080/api/v1/pitches"
curl "http://localhost:8080/api/v1/pitches?district=Центральный"
```

### Получить поле по id

`GET /api/v1/pitches/{id}`

Пример:

```bash
curl "http://localhost:8080/api/v1/pitches/1"
```

## Проверки

Тесты:

```bash
./mvnw test
```

Checkstyle:

```bash
./mvnw -DskipTests checkstyle:check
```

## SonarCloud

- Проект: https://sonarcloud.io/summary/new_code?id=SAKU102007_sem_4_java&branch=main
