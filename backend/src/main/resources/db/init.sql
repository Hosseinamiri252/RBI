CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    display_name VARCHAR(200),
    email VARCHAR(200),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    auth_source VARCHAR(10) NOT NULL DEFAULT 'LOCAL',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    max_concurrent_sessions INT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    last_login_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS browser_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id),
    container_id VARCHAR(100),
    container_name VARCHAR(200),
    vnc_port INT,
    guacamole_connection_id VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'STARTING',
    started_at TIMESTAMPTZ DEFAULT NOW(),
    ended_at TIMESTAMPTZ,
    recording_path VARCHAR(500),
    client_ip VARCHAR(45),
    user_agent TEXT
);

CREATE TABLE IF NOT EXISTS recordings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES browser_sessions(id),
    user_id UUID NOT NULL REFERENCES users(id),
    file_path VARCHAR(500) NOT NULL,
    file_size_bytes BIGINT,
    duration_seconds INT,
    minio_object_key VARCHAR(500),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS access_policies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    applies_to_role VARCHAR(20) DEFAULT 'USER',
    max_session_duration_minutes INT DEFAULT 480,
    allowed_url_patterns TEXT[],
    blocked_url_patterns TEXT[],
    max_concurrent_sessions INT DEFAULT 1,
    recording_enabled BOOLEAN DEFAULT TRUE,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50),
    resource_id UUID,
    details JSONB,
    client_ip VARCHAR(45),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON browser_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_status ON browser_sessions(status);
CREATE INDEX IF NOT EXISTS idx_recordings_session_id ON recordings(session_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at);

INSERT INTO users (username, password_hash, display_name, role, auth_source)
VALUES ('admin',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4oS.7DtiWi',
        'System Administrator',
        'ADMIN',
        'LOCAL')
ON CONFLICT (username) DO NOTHING;
