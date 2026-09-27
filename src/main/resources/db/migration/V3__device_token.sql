-- Phones that should buzz when a table at this store calls.
-- The FCM token is unique globally so the same device moving to another store
-- just updates store_id instead of leaving a stale subscription behind.

CREATE TABLE device_token (
    id         uuid          NOT NULL,
    store_id   uuid          NOT NULL,
    token      varchar(512)  NOT NULL,
    locale varchar(8) NOT NULL DEFAULT 'fr',
    updated_at timestamptz   NOT NULL DEFAULT now(),
    CONSTRAINT pk_device_token PRIMARY KEY (id),
    CONSTRAINT fk_device_token_store FOREIGN KEY (store_id) REFERENCES store (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX ux_device_token_token ON device_token (token);
CREATE INDEX ix_device_token_store_id ON device_token (store_id);
