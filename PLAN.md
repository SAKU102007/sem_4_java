# Проект: Football Pitch Marketplace Minsk (обновленный план)

## Краткое резюме
1. Добавляем сортировку по цене и по уровню в дополнение к фильтрам.
2. По уровню поддерживаем **две сортировки одновременно**:
- по текущему среднему уровню участников;
- по близости к моему уровню.
3. Поиск по времени делаем строгим: пример `завтра 19:00–21:00` означает полная доступность на весь интервал.
4. Аренда инвентаря остается опциональной: нужна не всем, фильтруется только по запросу.
5. Поддерживаем оба сценария игр:
- открытая игра на уже забронированный слот;
- сбор с нуля, потом бронь.
6. Для ЛР1 начинаем с каркаса и search API, но модель сразу совместима со всеми следующими лабораторными.

## Изменения API/контрактов
1. Поиск площадок:
- `GET /api/v1/venues/search`
- Параметры фильтра: `pitchType`, `priceFrom`, `priceTo`, `durationMinutes`, `skillMin`, `skillMax`, `district`, `metro`, `lat`, `lng`, `radiusKm`.
- Время: `desiredStartAt`, `desiredEndAt` (строгое совпадение доступности).
- Сортировка: `sort=price_asc|price_desc|avg_skill_asc|avg_skill_desc|distance_to_me_asc|distance_to_me_desc`.

2. Опциональный инвентарь в поиске:
- `needInventory=false` по умолчанию.
- Если `needInventory=true`, можно передать `ballQty`, `bibsQty`.
- В выдаче показываем наличие и цену инвентаря для конкретной площадки/слота.

3. Открытые игры (слот уже есть):
- `POST /api/v1/open-games` (создать набор на существующую бронь).
- `POST /api/v1/open-games/{id}/join`
- `POST /api/v1/open-games/{id}/auto-balance`

4. Сбор с нуля:
- `POST /api/v1/recruitments` (без слота, с параметрами игры).
- `POST /api/v1/recruitments/{id}/join`
- `POST /api/v1/recruitments/{id}/book` (организатор выбирает площадку/слот и бронирует).

## Модель данных (добавления)
1. `Pitch`:
- `type`, `surface`, `pricePerHour`, `district`, `metro`, `lat`, `lng`.

2. `Booking`:
- `startAt`, `endAt`, `status`, `lockExpiresAt`.
- Проверка пересечения по `[startAt, endAt)`.

3. `EquipmentOffer` (на площадку):
- `pitchId`, `itemType(BALL|BIBS)`, `stockTotal`, `rentFixedPrice`.

4. `OpenGame`:
- `bookingId`, `targetSkillMin`, `targetSkillMax`, `maxPlayers`, `status`.

5. `Recruitment` (сбор с нуля):
- `organizerId`, `desiredStartAt`, `desiredEndAt`, `pitchType`, `district/metro/radius`, `targetSkillMin/Max`, `minPlayers`, `maxPlayers`, `status(COLLECTING,READY_TO_BOOK,BOOKED,...)`.

6. `User`:
- `rating` (1..100), роли `PLAYER|VENUE_OWNER|ADMIN`.

## Правила реализации
1. Сортировка по среднему уровню:
- Для `open games`: средний рейтинг присоединившихся игроков.
- Для площадок: исторический средний рейтинг подтвержденных игр (агрегат).

2. Сортировка по близости к моему уровню:
- Метрика: `abs(myRating - currentGameAvgOrTargetMid)`.
- Доступна только авторизованному пользователю.

3. Время:
- Строгое совпадение: слот должен быть полностью свободен на всем интервале `desiredStartAt..desiredEndAt`.
- Часовой пояс: `Europe/Minsk`.

4. Инвентарь:
- Опционален, не влияет на поиск если `needInventory=false`.
- Цена инвентаря фиксированная за сессию, отдельно для каждой площадки.

5. Сбор с нуля:
- Сначала набор участников.
- После достижения минимума организатор выбирает слот и создает бронь.

## План реализации именно для ЛР1
1. Создать репозиторий с первыми файлами:
- `.gitignore`, `README.md` (до кода).
2. Поднять Spring Boot проект (Java 17, Maven), подключить Checkstyle.
3. Реализовать базовый модуль поиска площадок:
- `GET` с `@RequestParam` для фильтров/сортировки/времени;
- `GET /venues/{id}` с `@PathVariable`.
4. Слои: `Controller -> Service -> Repository`, DTO + mapper.
5. В seed добавить минимальные реальные данные по площадкам Минска.
6. Подготовить README с API-примерами (включая `завтра 19:00–21:00`).

## Тестовые сценарии (обязательные)
1. Фильтрация по типу площадки, цене, длительности, уровню.
2. Сортировка `price_asc/price_desc`.
3. Сортировка `avg_skill_*`.
4. Сортировка `distance_to_me_*` для авторизованного пользователя.
5. Строгое время: слот полностью свободен/частично занят.
6. Инвентарь как опциональный фильтр (`needInventory=true/false`).

## Зафиксированные допущения
1. Валюта: BYN.
2. Уровень игроков: шкала 1..100.
3. Повторные брони: только weekly.
4. Погода и онлайн-оплата из текущего scope исключены.
