CREATE CONSTANT USER_CREATED_AT VALUE TIMESTAMP '2026-01-01 10:00:00';
CREATE CONSTANT USER_ROLE_PLAYER VALUE 'PLAYER';
CREATE CONSTANT BOOKING_STATUS_CONFIRMED VALUE 'CONFIRMED';
CREATE CONSTANT BOOKING_NOT_RECURRING VALUE FALSE;
CREATE CONSTANT BOOKING_NO_LOCK_EXPIRES_AT VALUE CAST(NULL AS TIMESTAMP);
CREATE CONSTANT BOOKING_NO_RECURRENCE_GROUP VALUE CAST(NULL AS BIGINT);

INSERT INTO users (id, email, password_hash, role, rating, created_at) VALUES
    (1, 'player1@minsk.by', 'hash1', USER_ROLE_PLAYER, 35, USER_CREATED_AT),
    (2, 'player2@minsk.by', 'hash2', USER_ROLE_PLAYER, 55, USER_CREATED_AT),
    (3, 'player3@minsk.by', 'hash3', USER_ROLE_PLAYER, 72, USER_CREATED_AT),
    (4, 'player4@minsk.by', 'hash4', USER_ROLE_PLAYER, 88, USER_CREATED_AT),
    (5, 'owner1@minsk.by', 'hash5', 'VENUE_OWNER', 60, USER_CREATED_AT),
    (6, 'admin@minsk.by', 'hash6', 'ADMIN', 65, USER_CREATED_AT);

INSERT INTO venues (id, owner_user_id, name, address, active) VALUES
    (1, 5, 'Футбольный манеж Минск Арена', 'пр-т Победителей, 111', true),
    (2, 5, 'Олимпик Парк Футбол', 'ул. Тимирязева, 74А', true),
    (3, 5, 'Трактор Филд', 'ул. Ванеева, 3', true);

INSERT INTO pitches (id, venue_id, type, surface, price_per_hour, district, metro, lat, lng, average_skill, active) VALUES
    (1, 1, 'FIVE_FUTSAL', 'PARQUET', 120.00, 'Центральный', 'Немига', 53.923280, 27.515820, 70.0, true),
    (2, 1, 'FIVE_TURF', 'TURF', 90.00, 'Центральный', 'Фрунзенская', 53.907050, 27.530150, 52.0, true),
    (3, 2, 'EIGHT', 'TURF', 150.00, 'Советский', 'Московская', 53.937600, 27.623400, 80.0, true),
    (4, 3, 'ELEVEN', 'TURF', 200.00, 'Партизанский', 'Тракторный завод', 53.889200, 27.638300, 65.0, true),
    (5, 2, 'FIVE_FUTSAL', 'PARQUET', 110.00, 'Ленинский', 'Пролетарская', 53.884300, 27.585000, 58.0, true);

INSERT INTO equipment_offers (id, pitch_id, item_type, stock_total, rent_fixed_price) VALUES
    (1, 1, 'BALL', 8, 20.00),
    (2, 1, 'BIBS', 12, 15.00),
    (3, 2, 'BALL', 4, 15.00),
    (4, 2, 'BIBS', 6, 10.00),
    (5, 3, 'BALL', 6, 18.00),
    (6, 3, 'BIBS', 0, 12.00),
    (7, 4, 'BALL', 10, 30.00),
    (8, 4, 'BIBS', 20, 25.00),
    (9, 5, 'BALL', 2, 17.00),
    (10, 5, 'BIBS', 4, 9.00);

INSERT INTO bookings (id, pitch_id, player_id, start_at, end_at, status, lock_expires_at, recurring, recurrence_group_id) VALUES
    (1, 1, 1, TIMESTAMP '2026-03-01 18:30:00', TIMESTAMP '2026-03-01 20:00:00', BOOKING_STATUS_CONFIRMED, BOOKING_NO_LOCK_EXPIRES_AT, BOOKING_NOT_RECURRING, BOOKING_NO_RECURRENCE_GROUP),
    (2, 2, 2, TIMESTAMP '2026-03-01 16:00:00', TIMESTAMP '2026-03-01 17:00:00', BOOKING_STATUS_CONFIRMED, BOOKING_NO_LOCK_EXPIRES_AT, BOOKING_NOT_RECURRING, BOOKING_NO_RECURRENCE_GROUP),
    (3, 3, 3, TIMESTAMP '2026-03-01 21:00:00', TIMESTAMP '2026-03-01 22:00:00', BOOKING_STATUS_CONFIRMED, BOOKING_NO_LOCK_EXPIRES_AT, BOOKING_NOT_RECURRING, BOOKING_NO_RECURRENCE_GROUP),
    (4, 4, 4, TIMESTAMP '2026-03-01 19:00:00', TIMESTAMP '2026-03-01 21:00:00', 'CANCELLED', BOOKING_NO_LOCK_EXPIRES_AT, BOOKING_NOT_RECURRING, BOOKING_NO_RECURRENCE_GROUP),
    (5, 5, 2, TIMESTAMP '2026-03-01 19:30:00', TIMESTAMP '2026-03-01 21:00:00', 'LOCKED', TIMESTAMP '2026-03-01 19:35:00', BOOKING_NOT_RECURRING, BOOKING_NO_RECURRENCE_GROUP);
