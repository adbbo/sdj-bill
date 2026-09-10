CREATE TABLE users (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    username        VARCHAR(64)  NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    display_name    VARCHAR(64)  NOT NULL,
    company         VARCHAR(128) NOT NULL,
    role            VARCHAR(32)  NOT NULL,
    phone           VARCHAR(32),
    email           VARCHAR(128),
    merchant_id     VARCHAR(64),
    enabled         TINYINT(1)   NOT NULL DEFAULT 1,
    created_at      DATETIME(6)  NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tickets (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    ticket_no       VARCHAR(32)  NOT NULL UNIQUE,
    title           VARCHAR(200) NOT NULL,
    description     TEXT         NOT NULL,
    initiator_id    BIGINT       NOT NULL,
    handler_id      BIGINT       NULL,
    status          VARCHAR(32)  NOT NULL,
    priority        VARCHAR(32)  NOT NULL,
    category        VARCHAR(32)  NOT NULL,
    contact_phone   VARCHAR(32),
    contact_email   VARCHAR(128),
    merchant_id     VARCHAR(64),
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    resolved_at     DATETIME(6),
    CONSTRAINT fk_ticket_initiator FOREIGN KEY (initiator_id) REFERENCES users (id),
    CONSTRAINT fk_ticket_handler   FOREIGN KEY (handler_id)   REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ticket_events (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    ticket_id       BIGINT       NOT NULL,
    author_id       BIGINT       NULL,
    event_type      VARCHAR(32)  NOT NULL,
    content         TEXT         NOT NULL,
    created_at      DATETIME(6)  NOT NULL,
    CONSTRAINT fk_event_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id),
    CONSTRAINT fk_event_author FOREIGN KEY (author_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_tickets_status ON tickets (status);
CREATE INDEX idx_tickets_handler ON tickets (handler_id);
CREATE INDEX idx_tickets_created ON tickets (created_at);
CREATE INDEX idx_tickets_initiator ON tickets (initiator_id);
CREATE INDEX idx_events_ticket ON ticket_events (ticket_id, created_at);
