-- Waitless initial schema.
-- Flyway owns the schema; Hibernate runs with ddl-auto=validate against it.

CREATE TABLE account (
     id            uuid          NOT NULL,
     firebase_uid  varchar(128)  NOT NULL,
     full_name     varchar(255)  NOT NULL,
     email         varchar(255)        NOT NULL,
     phone_area    varchar(8),
     phone_number  varchar(32),
     created_at    timestamptz   NOT NULL DEFAULT now(),
     updated_at    timestamptz   NOT NULL DEFAULT now(),
     CONSTRAINT pk_account PRIMARY KEY (id)
);

CREATE UNIQUE INDEX ux_account_firebase_uid ON account (firebase_uid);
CREATE UNIQUE INDEX ux_account_email ON account (email);

CREATE TABLE store (
    id         uuid         NOT NULL,
    account_id   uuid         NOT NULL,
    name       varchar(255) NOT NULL,
    created_at timestamptz  NOT NULL,
    CONSTRAINT pk_store PRIMARY KEY (id),
    CONSTRAINT fk_store_account FOREIGN KEY (account_id) REFERENCES account (id) ON DELETE CASCADE
);

CREATE INDEX ix_store_account_id ON store (account_id);

CREATE TABLE store_table (
    id           uuid         NOT NULL,
    store_id     uuid         NOT NULL,
    table_number integer      NOT NULL,
    qr_token     varchar(64)  NOT NULL,
    CONSTRAINT pk_store_table PRIMARY KEY (id),
    CONSTRAINT fk_store_table_store FOREIGN KEY (store_id) REFERENCES store (id) ON DELETE CASCADE
);

CREATE INDEX ix_store_table_store_id ON store_table (store_id);
CREATE UNIQUE INDEX ux_store_table_qr_token ON store_table (qr_token);
-- A table number is unique within a store, but may repeat across stores.
CREATE UNIQUE INDEX ux_store_table_store_id_table_number ON store_table (store_id, table_number);

CREATE TABLE menu_item (
    id          uuid           NOT NULL,
    store_id    uuid           NOT NULL,
    category    varchar(255)   NOT NULL,
    name        varchar(255)   NOT NULL,
    description varchar(1000),
    price       numeric(10, 2) NOT NULL,
    CONSTRAINT pk_menu_item PRIMARY KEY (id),
    CONSTRAINT fk_menu_item_store FOREIGN KEY (store_id) REFERENCES store (id) ON DELETE CASCADE
);

CREATE INDEX ix_menu_item_store_id ON menu_item (store_id);

CREATE TABLE staff_member (
    id        uuid         NOT NULL,
    store_id  uuid         NOT NULL,
    full_name varchar(255) NOT NULL,
    pin_hash  varchar(255) NOT NULL,
    CONSTRAINT pk_staff_member PRIMARY KEY (id),
    CONSTRAINT fk_staff_member_store FOREIGN KEY (store_id) REFERENCES store (id) ON DELETE CASCADE
);

CREATE INDEX ix_staff_member_store_id ON staff_member (store_id);

CREATE TABLE service_request (
    id                 uuid        NOT NULL,
    store_id           uuid        NOT NULL,
    table_id           uuid        NOT NULL,
    type               varchar(32) NOT NULL,
    status             varchar(32) NOT NULL,
    payment_method     varchar(32),
    created_at         timestamptz NOT NULL,
    acknowledged_by_id uuid,
    acknowledged_at    timestamptz,
    resolved_at        timestamptz,
    CONSTRAINT pk_service_request PRIMARY KEY (id),
    CONSTRAINT fk_service_request_store FOREIGN KEY (store_id) REFERENCES store (id) ON DELETE CASCADE,
    CONSTRAINT fk_service_request_table FOREIGN KEY (table_id) REFERENCES store_table (id) ON DELETE CASCADE,
    CONSTRAINT fk_service_request_ack_by FOREIGN KEY (acknowledged_by_id) REFERENCES staff_member (id) ON DELETE SET NULL
);

CREATE INDEX ix_service_request_store_id ON service_request (store_id);
CREATE INDEX ix_service_request_table_id ON service_request (table_id);
CREATE INDEX ix_service_request_acknowledged_by_id ON service_request (acknowledged_by_id);
-- Drives GET /api/stores/{storeId}/requests/active.
CREATE INDEX ix_service_request_store_id_status ON service_request (store_id, status);
