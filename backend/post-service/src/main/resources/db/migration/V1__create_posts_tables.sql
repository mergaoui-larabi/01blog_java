-- Posts
CREATE TABLE
    posts (
        id UUID PRIMARY KEY,
        author_id UUID NOT NULL,
        content TEXT NOT NULL,
        likes_count BIGINT NOT NULL DEFAULT 0,
        comments_count BIGINT NOT NULL DEFAULT 0,
        hidden BOOLEAN NOT NULL DEFAULT FALSE,
        created_at TIMESTAMP
        WITH
            TIME ZONE NOT NULL,
            updated_at TIMESTAMP
        WITH
            TIME ZONE NOT NULL,
            CONSTRAINT posts_likes_count_check CHECK (likes_count >= 0),
            CONSTRAINT posts_comments_count_check CHECK (comments_count >= 0)
    );

CREATE INDEX idx_posts_author_id ON posts (author_id);

CREATE INDEX idx_posts_created_at ON posts (created_at);

-- Media associated with posts
CREATE TABLE
    post_medias (
        post_id UUID NOT NULL,
        media_id UUID NOT NULL,
        position INTEGER NOT NULL,
        PRIMARY KEY (post_id, media_id),
        CONSTRAINT fk_post_media_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
        CONSTRAINT post_media_position_check CHECK (position >= 0),
        CONSTRAINT post_media_post_position_key UNIQUE (post_id, position)
    );

CREATE INDEX idx_post_medias_media_id ON post_medias (media_id);

-- Post likes
CREATE TABLE
    likes (
        id UUID PRIMARY KEY,
        post_id UUID NOT NULL,
        user_id UUID NOT NULL,
        created_at TIMESTAMP
        WITH
            TIME ZONE NOT NULL,
            CONSTRAINT likes_post_id_user_id_key UNIQUE (post_id, user_id),
            CONSTRAINT fk_likes_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE
    );

CREATE INDEX idx_likes_user_id ON likes (user_id);

-- Comments
CREATE TABLE
    comments (
        id UUID PRIMARY KEY,
        post_id UUID NOT NULL,
        author_id UUID NOT NULL,
        content TEXT NOT NULL,
        created_at TIMESTAMP
        WITH
            TIME ZONE NOT NULL,
            updated_at TIMESTAMP
        WITH
            TIME ZONE NOT NULL,
            CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE
    );

CREATE INDEX idx_comments_created_at ON comments (created_at);