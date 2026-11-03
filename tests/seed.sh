#!/usr/bin/env bash
# Load the mock data from docs/database/seed into the five service databases,
# then write the placeholder image files the medias rows point at.
#
#   ./tests/seed.sh
#
# Every seed file truncates its own tables first, so re-running replaces the data
# instead of stacking on top of it. The databases have to be up already:
#
#   make docker-up
#
# Seeded accounts all use the password Password123!

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SEED_DIR="$REPO_ROOT/docs/database/seed"

# Container and its seed file, in the order the references make sense.
SEEDS=(
  "blog-postgres-users:01_users.sql"
  "blog-postgres-media:02_medias.sql"
  "blog-postgres-posts:03_posts.sql"
  "blog-postgres-notifications:04_notifications.sql"
  "blog-postgres-moderation:05_moderation.sql"
)

run_sql() {
  docker exec -i "$1" sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1 -At -f -'
}

for entry in "${SEEDS[@]}"; do
  container="${entry%%:*}"
  if [ "$(docker inspect -f '{{.State.Running}}' "$container" 2>/dev/null || echo false)" != "true" ]; then
    echo "$container is not running. Start the databases with: make docker-up" >&2
    exit 1
  fi
done

for entry in "${SEEDS[@]}"; do
  container="${entry%%:*}"
  file="${entry##*:}"
  echo "==> $container <- $file"
  run_sql "$container" < "$SEED_DIR/$file"
done

echo "==> placeholder media files"
python3 "$SEED_DIR/make_media_files.py"

echo
echo "==> row counts"
for entry in "${SEEDS[@]}"; do
  container="${entry%%:*}"
  printf '    %s\n' "$container"
  while read -r table; do
    count="$(printf 'select count(*) from %s;' "$table" | run_sql "$container")"
    printf '        %s = %s\n' "$table" "$count"
  done < <(printf "select tablename from pg_tables
                   where schemaname = 'public' and tablename not like 'flyway%%'
                   order by 1;" | run_sql "$container")
done
