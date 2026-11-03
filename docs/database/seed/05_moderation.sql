-- Mock data for the blog_moderations database: 100 reports and 100
-- moderation actions.
--
-- Reports cover all four statuses and both target types. Actions are only ever
-- recorded against reports that have left OPEN: the 60 non-open reports each get
-- one entry, and the 40 earliest of those get a follow-up entry, which is how a
-- real review history looks. Moderators are accounts 1-3, the seeded admins.
--
-- Apply with:
--   docker exec -i blog-postgres-moderation sh -c \
--     'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1 -f -' \
--     < docs/database/seed/05_moderation.sql

truncate table moderation_actions, reports;

insert into reports (
    id, reporter_id, target_type, target_id, reason, status,
    created_at, updated_at)
select
    md5('01blog-report-' || r)::uuid,
    md5('01blog-user-' || reporter_index)::uuid,
    case when r % 4 = 0 then 'USER' else 'POST' end,
    case
        when r % 4 = 0 then md5('01blog-user-' || target_index)::uuid
        else md5('01blog-post-' || ((r * 7) % 100) + 1)::uuid
    end,
    (array[
        'Repeated spam in the replies, same text on four posts.',
        'Post contains a link to a phishing site.',
        'Harassment directed at another account.',
        'Impersonating another user, same avatar and handle.',
        'Explicit content shown without a warning.',
        'Copyrighted screenshots reposted without credit.',
        'Automated account posting the same reply everywhere.',
        'Personal details of another user published without consent.'
    ])[1 + ((r * 5) % 8)],
    case
        when r <= 40 then 'OPEN'
        when r <= 70 then 'UNDER_REVIEW'
        when r <= 85 then 'RESOLVED'
        else 'DISMISSED'
    end,
    created_at,
    created_at + make_interval(hours => r % 24)
from (
    select
        r,
        ((r * 3) % 100) + 1 as reporter_index,
        case
            when ((r * 11) % 100) + 1 = ((r * 3) % 100) + 1
                then (((r * 11) % 100) + 1) % 100 + 1
            else ((r * 11) % 100) + 1
        end as target_index,
        now() - make_interval(hours => (100 - r) * 8, mins => (r * 29) % 60) as created_at
    from generate_series(1, 100) as r
) as seeded;

with actionable as (
    select
        id,
        status,
        created_at,
        row_number() over (order by created_at) as rn
    from reports
    where status <> 'OPEN'
)
insert into moderation_actions (
    id, report_id, moderator_id, action_type, details, created_at)
select
    md5('01blog-moderation-action-' || m)::uuid,
    a.id,
    md5('01blog-user-' || (m % 3) + 1)::uuid,
    'STATUS_CHANGED',
    case
        when m <= 60
            then 'Status set to ' || lower(replace(a.status, '_', ' ')) || ' after review.'
        else 'Re-reviewed with the reporter, status left at '
            || lower(replace(a.status, '_', ' ')) || '.'
    end,
    a.created_at + make_interval(hours => case when m <= 60 then 6 + (m % 24) else 30 + (m % 24) end)
from generate_series(1, 100) as m
join actionable as a on a.rn = case when m <= 60 then m else m - 60 end;
