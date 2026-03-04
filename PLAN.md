# Проект: Football Pitch Marketplace Minsk (JPA-блок)

## Цель текущего этапа
Сфокусироваться на лабораторной по JPA/Hibernate и сократить модель до минимально достаточной:
- закрыть требования по `OneToMany`, `ManyToMany`, CRUD, `N+1`, транзакциям;
- не усложнять проект лишними сущностями на этом этапе;
- использовать `LAZY` для всех связей.

## Зафиксированное сокращение сущностей (5 штук)
1. `User`
- `id`, `name`, `rating`, `role`.

2. `Pitch`
- `id`, `name`, `type`, `district`, `metro`, `pricePerHour`.

3. `Booking`
- `id`, `pitchId`, `organizerId`, `startAt`, `endAt`, `status`.

4. `OpenGame`
- `id`, `bookingId`, `organizerId`, `targetSkillMin`, `targetSkillMax`, `maxPlayers`, `status`.

5. `EquipmentOffer`
- `id`, `pitchId`, `itemType`, `stockTotal`, `rentFixedPrice`.

## Связи (PK/FK и кратности)
1. `Pitch (1) -> (N) Booking`
- FK: `booking.pitch_id -> pitch.id`.
- Тип: `@OneToMany(mappedBy = "pitch", fetch = LAZY)`.

2. `Pitch (1) -> (N) EquipmentOffer`
- FK: `equipment_offer.pitch_id -> pitch.id`.
- Тип: `@OneToMany(mappedBy = "pitch", fetch = LAZY)`.

3. `User (1) -> (N) Booking` (организатор брони)
- FK: `booking.organizer_id -> user.id`.
- Тип: `@ManyToOne(fetch = LAZY)` в `Booking`.

4. `Booking (1) -> (1) OpenGame`
- FK: `open_game.booking_id -> booking.id` (unique).
- Тип: `@OneToOne(fetch = LAZY)` в `OpenGame`.

5. `User (N) <-> (N) OpenGame` (участники)
- Join table: `open_game_participants(open_game_id, user_id)`.
- Тип: `@ManyToMany(fetch = LAZY)`.

6. `User (1) -> (N) OpenGame` (организатор игры)
- FK: `open_game.organizer_id -> user.id`.
- Тип: `@ManyToOne(fetch = LAZY)` в `OpenGame`.

## Что исключаем из текущего scope
1. `Recruitment` (сбор с нуля) переносим в backlog.
2. Дополнительные сущности статистики/аудита пока не добавляем.
3. Расширение модели делаем только после закрытия всех пунктов JPA-блока.

## Правила реализации JPA (для следующего шага)
1. Все ассоциации явно помечаем `fetch = FetchType.LAZY`.
2. `CascadeType` используем только там, где объект является жизненно зависимым:
- `Pitch -> EquipmentOffer`: `PERSIST, MERGE, REMOVE`.
- Для `ManyToMany` участников без `REMOVE` cascade.
3. Демонстрация `N+1`: список `OpenGame` + чтение `participants` в цикле.
4. Решение `N+1`: `@EntityGraph(attributePaths = "participants")` или `fetch join`.
5. Демонстрация транзакций:
- сервис без `@Transactional`: показываем частичное сохранение;
- сервис с `@Transactional`: показываем полный rollback при исключении.

## Зафиксированные допущения
1. Валюта: BYN.
2. Уровень игроков: шкала `1..100`.
3. Часовой пояс: `Europe/Minsk`.
