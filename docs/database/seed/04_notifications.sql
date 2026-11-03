-- Mock data for the blog_notifications database: 100 NEW_POST notifications.
--
-- The actor is the author of the post named in resource_id, the recipient is a
-- different account (the table rejects self-notification), and one third of the
-- rows are already read so the unread badge has something to count.
--
-- The adjective and noun arrays below mirror 01_users.sql so the handle in the
-- message matches the seeded username of the actor.
--
-- Apply with:
--   docker exec -i blog-postgres-notifications sh -c \
--     'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1 -f -' \
--     < docs/database/seed/04_notifications.sql

truncate table notifications;

with post_of as (
    select n, ((n * 7) % 100) + 1 as post_index
    from generate_series(1, 100) as n
), actor_of as (
    select n, post_index, ((post_index * 3) % 100) + 1 as actor_index
    from post_of
), recipient_of as (
    select
        n,
        post_index,
        actor_index,
        case
            when ((actor_index + n - 1) % 100) + 1 = actor_index
                then (actor_index % 100) + 1
            else ((actor_index + n - 1) % 100) + 1
        end as recipient_index
    from actor_of
)
insert into notifications (
    id, recipient_id, actor_id, type, resource_id, message,
    read, read_at, created_at, updated_at)
select
    md5('01blog-notification-' || n)::uuid,
    md5('01blog-user-' || recipient_index)::uuid,
    md5('01blog-user-' || actor_index)::uuid,
    'NEW_POST',
    md5('01blog-post-' || post_index)::uuid,
    '@' || lower(
        (array[
            'swift', 'lucid', 'quiet', 'bold', 'amber',
            'cosmic', 'nimble', 'crisp', 'vivid', 'mellow',
            'rustic', 'sleek', 'earnest', 'playful', 'curious',
            'diligent', 'electric', 'gentle', 'hollow', 'solar'
        ])[1 + ((actor_index - 1) % 20)] || '_' ||
        (array[
            'otter', 'falcon', 'cactus', 'harbor', 'kernel',
            'lantern', 'meadow', 'nebula', 'orbit', 'pebble',
            'quartz', 'river', 'sparrow', 'thistle', 'umber',
            'vertex', 'willow', 'zephyr', 'beacon', 'compass'
        ])[1 + ((actor_index * 7) % 20)] || '_' || actor_index
    ) || ' published a new post',
    n % 3 = 0,
    case when n % 3 = 0
        then created_at + make_interval(hours => 1 + (n % 48))
    end,
    created_at,
    case when n % 3 = 0
        then created_at + make_interval(hours => 1 + (n % 48))
        else created_at
    end
from (
    select
        n,
        post_index,
        actor_index,
        recipient_index,
        now() - make_interval(hours => (100 - n) * 5, mins => (n * 13) % 60) as created_at
    from recipient_of
) as seeded;
