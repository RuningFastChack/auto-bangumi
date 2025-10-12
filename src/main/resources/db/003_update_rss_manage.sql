CREATE TABLE "rss_manage_new"
(
    "id"                integer NOT NULL COLLATE BINARY PRIMARY KEY AUTOINCREMENT,
    "official_title"    text,
    "official_title_en" text,
    "official_title_jp" text,
    "season"            integer DEFAULT 0,
    "status"            text,
    "filter"            text,
    "poster_link"       text,
    "save_path"         text,
    "deleted"           integer DEFAULT 0,
    "complete"          text,
    "update_week"       integer,
    "send_date"         text,
    "rss_list"          text,
    "config"            text
);

INSERT INTO rss_manage_new (id, official_title, official_title_en, official_title_jp, season, status, filter,
                            poster_link, save_path, deleted, complete,
                            update_week, send_date, rss_list, config)
SELECT id,
       official_title,
       official_title,
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
       rss_list,
       config
FROM rss_manage;

DROP TABLE rss_manage;

ALTER TABLE rss_manage_new
    RENAME TO rss_manage;