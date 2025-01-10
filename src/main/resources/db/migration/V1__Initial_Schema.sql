CREATE TABLE rooms (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    capacity INT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE participants (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    room_id BIGINT REFERENCES rooms(id),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE event_participants (
    event_id BIGINT REFERENCES events(id),
    participant_id BIGINT REFERENCES participants(id),
    PRIMARY KEY (event_id, participant_id)
);