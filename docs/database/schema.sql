CREATE TABLE "users" (
  "id" integer PRIMARY KEY,
  "username" varchar UNIQUE NOT NULL,
  "email" varchar UNIQUE NOT NULL,
  "password_hash" text NOT NULL,
  "role" varchar,
  "is_banned" boolean DEFAULT false,
  "created_at" timestamp
);

CREATE TABLE "subscriptions" (
  "id" integer PRIMARY KEY,
  "subscriber_id" integer NOT NULL,
  "target_user_id" integer NOT NULL,
  "created_at" timestamp
);

CREATE TABLE "posts" (
  "id" integer PRIMARY KEY,
  "title" text NOT NULL,
  "user_id" integer NOT NULL,
  "description" text,
  "created_at" timestamp,
  "updated_at" timestamp
);

CREATE TABLE "post_media" (
  "id" integer PRIMARY KEY,
  "post_id" integer NOT NULL,
  "media_url" text NOT NULL,
  "media_type" varchar
);

CREATE TABLE "likes" (
  "id" integer PRIMARY KEY,
  "user_id" integer NOT NULL,
  "post_id" integer NOT NULL,
  "created_at" timestamp
);

CREATE TABLE "comments" (
  "id" integer PRIMARY KEY,
  "user_id" integer NOT NULL,
  "post_id" integer NOT NULL,
  "content" text NOT NULL,
  "created_at" timestamp,
  "updated_at" timestamp
);

CREATE TABLE "notifications" (
  "id" integer PRIMARY KEY,
  "user_id" integer NOT NULL,
  "type" varchar,
  "reference_id" integer,
  "is_read" boolean DEFAULT false,
  "created_at" timestamp,
  "updated_at" timestamp
);

CREATE TABLE "reports" (
  "id" integer PRIMARY KEY,
  "reporter_id" integer NOT NULL,
  "reported_user_id" integer NOT NULL,
  "reason" text NOT NULL,
  "status" varchar,
  "created_at" timestamp,
  "updated_at" timestamp
);

CREATE TABLE "admin_actions" (
  "id" integer PRIMARY KEY,
  "admin_id" integer NOT NULL,
  "action_type" varchar,
  "target_user_id" integer,
  "target_post_id" integer,
  "created_at" timestamp
);

COMMENT ON COLUMN "users"."role" IS 'USER or ADMIN';

COMMENT ON COLUMN "post_media"."media_type" IS 'IMAGE or VIDEO';

COMMENT ON COLUMN "notifications"."type" IS 'NEW_POST, LIKE, COMMENT, SUBSCRIBE';

COMMENT ON COLUMN "notifications"."reference_id" IS 'post_id or user_id';

COMMENT ON COLUMN "reports"."status" IS 'PENDING, REVIEWED, RESOLVED';

COMMENT ON COLUMN "admin_actions"."action_type" IS 'BAN_USER, DELETE_POST, WARN_USER';

ALTER TABLE "subscriptions" ADD FOREIGN KEY ("subscriber_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "subscriptions" ADD FOREIGN KEY ("target_user_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "posts" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "post_media" ADD FOREIGN KEY ("post_id") REFERENCES "posts" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "likes" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "likes" ADD FOREIGN KEY ("post_id") REFERENCES "posts" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "comments" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "comments" ADD FOREIGN KEY ("post_id") REFERENCES "posts" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "notifications" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "reports" ADD FOREIGN KEY ("reporter_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "reports" ADD FOREIGN KEY ("reported_user_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "admin_actions" ADD FOREIGN KEY ("admin_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "admin_actions" ADD FOREIGN KEY ("target_user_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "admin_actions" ADD FOREIGN KEY ("target_post_id") REFERENCES "posts" ("id") DEFERRABLE INITIALLY IMMEDIATE;
