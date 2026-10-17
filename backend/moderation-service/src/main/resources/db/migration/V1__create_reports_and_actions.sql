-- Reports
CREATE TABLE reports (
    id UUID PRIMARY KEY,
    reporter_id UUID NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id UUID NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE reports
    ADD CONSTRAINT reports_status_check CHECK (status IN ('OPEN', 'UNDER_REVIEW', 'RESOLVED', 'DISMISSED')),
    ADD CONSTRAINT reports_target_type_check CHECK (target_type IN ('USER', 'POST'));

CREATE INDEX idx_reports_reporter_id ON reports (reporter_id);
CREATE INDEX idx_reports_status ON reports (status);
CREATE INDEX idx_reports_target_type ON reports (target_type);
CREATE INDEX idx_reports_target_id ON reports (target_id);
CREATE INDEX idx_reports_created_at ON reports (created_at);

-- Moderation actions
CREATE TABLE moderation_actions (
    id UUID PRIMARY KEY,
    report_id UUID NOT NULL,
    moderator_id UUID NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE moderation_actions
    ADD CONSTRAINT moderation_actions_report_id_fkey FOREIGN KEY (report_id)
        REFERENCES reports (id) ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE moderation_actions
    ADD CONSTRAINT moderation_actions_action_type_check CHECK (action_type IN ('DISMISS', 'HIDE_POST', 'BAN_USER'));

CREATE INDEX idx_moderation_actions_report_id ON moderation_actions (report_id);
CREATE INDEX idx_moderation_actions_created_at ON moderation_actions (created_at);
