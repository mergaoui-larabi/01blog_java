-- Mock data for the blog_post database: 100 posts, 65 attachments, 100 likes
-- and 100 comments. post_medias is the @ElementCollection join table behind
-- Post.mediaIds, so it gets one row per attachment rather than a round 100.
--
-- Author ids point at the users seeded in 01_users.sql, media ids at the
-- accepted post images in 02_medias.sql. likes_count and comments_count are
-- recomputed from the rows below at the end of this file.
--
-- Apply with:
--   docker exec -i blog-postgres-posts sh -c \
--     'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1 -f -' \
--     < docs/database/seed/03_posts.sql

truncate table post_medias, likes, comments, posts;

insert into posts (
    id, author_id, content, likes_count, comments_count, hidden,
    created_at, updated_at)
select
    md5('01blog-post-' || j)::uuid,
    md5('01blog-user-' || ((j * 3) % 100) + 1)::uuid,
    (array[
        'I spent the weekend rewriting the notification pipeline and it finally makes sense.',
        'Something to keep in mind before you add a second database to your stack.',
        'Debugging a race condition that only showed up under load taught me more than any talk.',
        'The boring parts of a codebase are the ones worth understanding first.',
        'A short note on why I stopped using DTOs everywhere.',
        'Halfway through the migration and the old service is still serving traffic.',
        'Every time I touch the media service I learn something new about streaming responses.',
        'I used to think caching was a feature, now I think of it as a contract.',
        'Postgres handled the workload long before the queue did.',
        'Reading the moderation queue this week changed how I write copy.',
        'Two hours of profiling saved me a week of guessing.',
        'The follow graph is small enough to hold in your head, which is a feature.'
    ])[1 + ((j - 1) % 12)] || ' ' || (array[
        'The fix turned out to be one index and a careful look at the query plan.',
        'What surprised me was how much of the work was naming things well.',
        'Most of the complexity came from a schema that nobody owned.',
        'I keep coming back to the same lesson: model the workflow, not the table.',
        'Once the boundaries were clear the tests wrote themselves.',
        'The failing case was a null that only appeared on the second retry.',
        'Logs told the whole story, metrics only told half of it.',
        'A weekend of reading the spec beat a month of trial and error.',
        'The old path stayed in place until the new one had proven itself.',
        'Small services make the failure modes easier to reason about.',
        'It helped to write the response shape before the request shape.',
        'Rough edges show up in the parts users never see.'
    ])[1 + ((j * 5) % 12)] || ' ' || (array[
        'Happy to hear how you handled the same problem.',
        'Worth writing down before I forget it again.',
        'Next up is cleaning the retry logic.',
        'Still not sure this is the right call.',
        'Leaving the notes in the repo for whoever picks it up.',
        'Two more things to try before I move on.',
        'That is all for this week.',
        'Feedback welcome as always.'
    ])[1 + ((j * 7) % 8)] || ' ' || (array[
        '#buildlog', '#postgres', '#angular', '#springboot',
        '#testing', '#performance', '#codereview', '#design'
    ])[1 + (j % 8)],
    0,
    0,
    j in (88, 95),
    created_at,
    created_at + make_interval(mins => (j * 17) % 90)
from (
    select
        j,
        now() - make_interval(hours => (100 - j) * 6, mins => (j * 7) % 60) as created_at
    from generate_series(1, 100) as j
) as seeded;

-- Attach the 65 accepted post images to the newest 50 posts; the last 15 images
-- become a second slide on the post that already has one.
insert into post_medias (post_id, media_id, position)
select
    md5('01blog-post-' || (51 + ((k - 1) % 50)))::uuid,
    md5('01blog-post-media-' || k)::uuid,
    row_number() over (partition by (k - 1) % 50 order by k) - 1
from generate_series(1, 65) as k;

-- 100 likes over 100 posts. The post slot comes from hashtext so engagement is
-- uneven, the way it is in a real feed, instead of exactly one like each. The
-- liker index stays unique per row, which keeps (post_id, user_id) unique.
insert into likes (id, post_id, user_id, created_at)
select
    md5('01blog-like-' || l)::uuid,
    p.id,
    md5('01blog-user-' || ((l * 13) % 100) + 1)::uuid,
    p.created_at + make_interval(mins => 5 + (l * 11) % 1440)
from generate_series(1, 100) as l
join posts as p
    on p.id = md5('01blog-post-' || (1 + abs(hashtext('01blog-like-slot-' || l)::bigint) % 100))::uuid;

insert into comments (id, post_id, author_id, content, created_at, updated_at)
select
    md5('01blog-comment-' || c)::uuid,
    p.id,
    md5('01blog-user-' || ((c * 17) % 100) + 1)::uuid,
    (array[
        'This matches what I saw last sprint, thanks for writing it up.',
        'The index point is easy to miss until it bites you.',
        'Did you try measuring before the rewrite or after?',
        'We hit the same wall with the retry logic on the media service.',
        'Good reminder that the schema is not the model.',
        'Reading this on the train but it already made sense.',
        'Curious how this holds up once the queue is in front.',
        'Naming really is most of the work.',
        'The null on the second retry is a classic.',
        'Bookmarking this for the next review.',
        'Small services, big paperwork.',
        'Agreed, write the response first and the rest follows.'
    ])[1 + ((c * 5) % 12)],
    p.created_at + make_interval(mins => 15 + (c * 23) % 2880),
    p.created_at + make_interval(mins => 15 + (c * 23) % 2880)
from generate_series(1, 100) as c
join posts as p
    on p.id = md5('01blog-post-' || (1 + abs(hashtext('01blog-comment-slot-' || c)::bigint) % 100))::uuid;

-- Keep the denormalised counters honest.
update posts
set likes_count = (select count(*) from likes where likes.post_id = posts.id),
    comments_count = (select count(*) from comments where comments.post_id = posts.id);
