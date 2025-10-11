CREATE TABLE "rss_manage_new"
(
    "id"             integer NOT NULL COLLATE BINARY PRIMARY KEY AUTOINCREMENT,
    "official_title" text,
    "season"         integer DEFAULT 0,
    "status"         text,
    "filter"         text,
    "poster_link"    text,
    "save_path"      text,
    "deleted"        integer DEFAULT 0,
    "complete"       text,
    "update_week"    integer,
    "send_date"      text,
    "rss_list"       text,
    "config"         text
);

INSERT INTO rss_manage_new (id, official_title, season, status, filter,
                            poster_link, save_path, deleted, complete,
                            update_week, send_date, rss_list)
SELECT id,
       official_title,
       season,
       status,
       filter,
       poster_link,
       save_path,
       deleted,
       complete,
       update_week,
       send_date,
       rss_list
FROM rss_manage;

DROP TABLE rss_manage;

ALTER TABLE rss_manage_new
    RENAME TO rss_manage;

UPDATE rss_manage
SET "config" = '{"latestEpisode":"0","totalEpisode":"0"}'
WHERE "id" IS NOT NULL;