-- Mock data for the blog_users database: 100 users and 100 follows.
--
-- Every identifier is derived with md5() from a fixed seed string, so each
-- service reproduces the same UUID and rows living in different databases
-- reference each other correctly. Keep the seed strings stable when editing.
--
-- Password for every seeded account: Password123!  (BCrypt, cost 10)
-- Accounts 1-3 are ADMIN, accounts 97 and 98 are banned, accounts 1-30 have an
-- avatar (see 02_medias.sql).
--
-- status is the account-deletion state machine (ACTIVE, DELETION_PENDING,
-- DELETED). Every seeded account is ACTIVE: DELETION_PENDING is a transient
-- state that User Service leaves only after every cleanup call succeeds, and a
-- DELETED account has its posts, comments and reports anonymized in the other
-- services, which this data set does not reproduce.
--
-- Apply with:
--   docker exec -i blog-postgres-users sh -c \
--     'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1 -f -' \
--     < docs/database/seed/01_users.sql

truncate table followers, users;

insert into users (
    id, email, username, password_hash, age, gender, role,
    avatar_media_id, banned, status, created_at, updated_at)
select
    md5('01blog-user-' || i)::uuid,
    lower(adjective || '_' || noun || '_' || i) || '@mailinator.com',
    lower(adjective || '_' || noun || '_' || i),
    '$2a$10$JHrjW1djO7sKvx5YGm34eu5XCzsYysdCRUy14CNnrx906MsznMRsm',
    12 + (i * 7) % 89,
    case when i % 2 = 0 then 'FEMALE' else 'MALE' end,
    case when i <= 3 then 'ADMIN' else 'USER' end,
    case when i <= 30 then md5('01blog-avatar-media-' || i)::uuid end,
    i in (97, 98),
    'ACTIVE',
    created_at,
    created_at + make_interval(mins => (i * 13) % 600)
from (
    select
        i,
        (array[
            'swift', 'lucid', 'quiet', 'bold', 'amber',
            'cosmic', 'nimble', 'crisp', 'vivid', 'mellow',
            'rustic', 'sleek', 'earnest', 'playful', 'curious',
            'diligent', 'electric', 'gentle', 'hollow', 'solar'
        ])[1 + ((i - 1) % 20)] as adjective,
        (array[
            'otter', 'falcon', 'cactus', 'harbor', 'kernel',
            'lantern', 'meadow', 'nebula', 'orbit', 'pebble',
            'quartz', 'river', 'sparrow', 'thistle', 'umber',
            'vertex', 'willow', 'zephyr', 'beacon', 'compass'
        ])[1 + ((i * 7) % 20)] as noun,
        now() - make_interval(days => (100 - i) * 4, hours => i % 24) as created_at
    from generate_series(1, 100) as i
) as seeded;

-- Follows: every account follows one other account, never itself.
insert into followers (follower_id, followed_id, created_at, updated_at)
select
    md5('01blog-user-' || follower_index)::uuid,
    md5('01blog-user-' || case
        when followed_index = follower_index then followed_index % 100 + 1
        else followed_index
    end)::uuid,
    created_at,
    created_at
from (
    select
        ((i * 3) % 100) + 1 as follower_index,
        ((i * 29) % 100) + 1 as followed_index,
        now() - make_interval(days => (100 - i) * 2, hours => i % 24) as created_at
    from generate_series(1, 100) as i
) as pairs;
