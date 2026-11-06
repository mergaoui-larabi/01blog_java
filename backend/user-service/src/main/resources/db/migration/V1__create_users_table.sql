-- users
CREATE TABLE
    users (
        id UUID PRIMARY KEY,
        email VARCHAR(255) NOT NULL UNIQUE,
        username VARCHAR(100) NOT NULL UNIQUE,
        password_hash VARCHAR(255) NOT NULL,
        age SMALLINT NOT NULL,
        gender VARCHAR(30) NOT NULL,
        role VARCHAR(50) NOT NULL,
        avatar_media_id UUID,
        banned BOOLEAN NOT NULL DEFAULT FALSE,
        -- Account deletion is coordinated across services, so an account stays reachable in a
        -- DELETION_PENDING state until every cleanup call succeeds.
        status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
        created_at TIMESTAMP
        WITH
            TIME ZONE NOT NULL,
            updated_at TIMESTAMP
        WITH
            TIME ZONE NOT NULL,
            CONSTRAINT chk_users_account_status CHECK (
                status IN ('ACTIVE', 'DELETION_PENDING', 'DELETED')
            )
    );

-- followers
CREATE TABLE
    followers (
        follower_id UUID NOT NULL,
        followed_id UUID NOT NULL,
        created_at TIMESTAMP
        WITH
            TIME ZONE NOT NULL,
            updated_at TIMESTAMP
        WITH
            TIME ZONE NOT NULL,
            CONSTRAINT followers_pkey PRIMARY KEY (follower_id, followed_id),
            CONSTRAINT fk_followers_follower FOREIGN KEY (follower_id) REFERENCES users (id) ON DELETE CASCADE,
            CONSTRAINT fk_followers_followed FOREIGN KEY (followed_id) REFERENCES users (id) ON DELETE CASCADE,
            CONSTRAINT followers_no_self_follow CHECK (follower_id <> followed_id)
    );

CREATE INDEX idx_users_created_at ON users (created_at);

CREATE INDEX idx_followers_followed_id ON followers (followed_id);

CREATE INDEX idx_users_status ON users (status);