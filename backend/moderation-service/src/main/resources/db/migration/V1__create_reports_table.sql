CREATE TABLE
    reports (
        id UUID PRIMARY KEY,
        reporter_id UUID NOT NULL,
        target_type VARCHAR(20) NOT NULL CHECK (target_type IN ('USER', 'POST')),
        target_id UUID NOT NULL,
        reason TEXT NOT NULL CHECK (length (trim(reason)) > 0),
        status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (
            status IN ('OPEN', 'UNDER_REVIEW', 'RESOLVED', 'DISMISSED')
        ),
        created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX idx_reports_status_created_at ON reports (status, created_at);

CREATE INDEX idx_reports_target ON reports (target_type, target_id);

CREATE INDEX idx_reports_reporter_id ON reports (reporter_id);

CREATE TABLE
    moderation_actions (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        report_id UUID NOT NULL REFERENCES reports (id) ON DELETE RESTRICT,
        moderator_id UUID NOT NULL,
        action_type VARCHAR(50) NOT NULL CHECK (length (trim(action_type)) > 0),
        details TEXT,
        created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX idx_moderation_actions_report_id ON moderation_actions (report_id);

CREATE INDEX idx_moderation_actions_moderator_id ON moderation_actions (moderator_id);