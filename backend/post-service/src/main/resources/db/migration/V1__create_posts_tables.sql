-- Posts
CREATE TABLE posts (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL UNIQUE,
    author_id UUID NOT NULL,
    content TEXT NOT NULL,
    post_media_id UUID,
    likes_count BIGINT NOT NULL DEFAULT 0,
    comments_count BIGINT NOT NULL DEFAULT 0,
    is_hidden BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE posts
    ADD CONSTRAINT posts_likes_count_check CHECK (likes_count >= 0),
    ADD CONSTRAINT posts_comments_count_check CHECK (comments_count >= 0);

CREATE INDEX idx_posts_author_id ON posts (author_id);
CREATE INDEX idx_posts_created_at ON posts (created_at);

-- Post likes
CREATE TABLE likes (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE likes
    ADD CONSTRAINT likes_post_id_user_id_key UNIQUE (post_id, user_id);

CREATE INDEX idx_likes_post_id ON likes (post_id);
CREATE INDEX idx_likes_user_id ON likes (user_id);

-- Comments
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    post_id UUID NOT NULL,
    author_id UUID NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_comments_post_id ON comments (post_id);
CREATE INDEX idx_comments_created_at ON comments (created_at);
