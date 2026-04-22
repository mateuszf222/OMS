CREATE TABLE outbox_events (
                               id UUID PRIMARY KEY,
                               aggregate_type VARCHAR(255) NOT NULL,
                               aggregate_id VARCHAR(255) NOT NULL,
                               event_type VARCHAR(255) NOT NULL,
                               payload JSONB NOT NULL,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                               processed BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_outbox_processed_created ON outbox_events(processed, created_at);