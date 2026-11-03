-- Mock data for the blog_medias database: 100 media rows.
--
-- 30 avatars (attached to accounts 1-30 in 01_users.sql), 65 accepted post
-- images (attached to posts in 03_posts.sql) and 5 rejected uploads that belong
-- to no post, the way a rejected upload would in real use.
--
-- storage_key mirrors what MediaService.buildStorageKey produces:
-- <avatars|posts>/<owner uuid>/<uuid>.<extension>. The files themselves are
-- written by make_media_files.py next to this file, which also corrects
-- size_bytes to the real on-disk size.
--
-- Apply with:
--   docker exec -i blog-postgres-media sh -c \
--     'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1 -f -' \
--     < docs/database/seed/02_medias.sql

truncate table medias;

-- Avatars.
insert into medias (
    id, owner_id, purpose, status, original_filename, storage_key,
    detected_content_type, size_bytes, width, height, duration_ms,
    created_at, updated_at)
select
    md5('01blog-avatar-media-' || i)::uuid,
    md5('01blog-user-' || i)::uuid,
    'AVATAR_IMAGE',
    'ACCEPTED',
    'avatar_' || lpad(i::text, 2, '0') || '.png',
    'avatars/' || md5('01blog-user-' || i)::text || '/' || md5('01blog-avatar-media-' || i)::text || '.png',
    'image/png',
    4096,
    512,
    512,
    null,
    created_at,
    created_at
from (
    select
        i,
        now() - make_interval(days => (100 - i) * 4, hours => i % 24) as created_at
    from generate_series(1, 30) as i
) as seeded;

-- Post images: 65 accepted and attached to posts, 5 rejected and unused.
insert into medias (
    id, owner_id, purpose, status, original_filename, storage_key,
    detected_content_type, size_bytes, width, height, duration_ms,
    created_at, updated_at)
select
    md5('01blog-post-media-' || k)::uuid,
    md5('01blog-user-' || owner_index)::uuid,
    'POST_IMAGE',
    case when k > 65 then 'REJECTED' else 'ACCEPTED' end,
    'photo_' || lpad(k::text, 3, '0') || '.png',
    'posts/' || md5('01blog-user-' || owner_index)::text || '/' || md5('01blog-post-media-' || k)::text || '.png',
    'image/png',
    4096,
    (array[1024, 800, 1200, 640, 900])[1 + (k % 5)],
    (array[768, 800, 675, 480, 1200])[1 + (k % 5)],
    null,
    created_at,
    created_at
from (
    select
        k,
        -- Image k is attached to post 51 + ((k - 1) % 50) by 03_posts.sql, and an
        -- upload belongs to the author of the post it ends up in, whose index is
        -- ((post * 3) % 100) + 1. Keep the two files in step.
        ((51 + ((k - 1) % 50)) * 3) % 100 + 1 as owner_index,
        now() - make_interval(hours => (100 - k) * 6, mins => (k * 11) % 60) as created_at
    from generate_series(1, 70) as k
) as seeded;
