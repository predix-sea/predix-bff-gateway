CREATE TABLE IF NOT EXISTS audit_log (
    id              BIGSERIAL PRIMARY KEY,
    trace_id        VARCHAR(64) NOT NULL,
    event_type      VARCHAR(64) NOT NULL,
    actor_wallet    VARCHAR(128),
    client_ip       VARCHAR(64),
    country_code    VARCHAR(8),
    resource        VARCHAR(256),
    action          VARCHAR(128),
    outcome         VARCHAR(32) NOT NULL,
    detail_json     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_log_trace_id ON audit_log(trace_id);
CREATE INDEX idx_audit_log_event_type ON audit_log(event_type);
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at);
