CREATE TABLE outbox_events (
                               id UUID PRIMARY KEY,
                               aggregate_type VARCHAR(255) NOT NULL,
                               aggregate_id VARCHAR(255) NOT NULL,
                               event_type VARCHAR(255) NOT NULL,
                               payload JSONB NOT NULL,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                               processed BOOLEAN NOT NULL DEFAULT FALSE
);

-- Indeks częściowy (Partial Index) – skanuje tylko nieprzetworzone rekordy, co jest kluczowe dla wydajności przy rosnącej tabeli.
CREATE INDEX idx_outbox_unprocessed ON outbox_events(created_at) WHERE processed = false;