CREATE TABLE "rss_item_new"
(
    "id"                integer NOT NULL COLLATE BINARY PRIMARY KEY AUTOINCREMENT,
    "torrent_code"      text,
    "episode_num"       integer DEFAULT 0,
    "rss_manage_id"     integer,
    "translation_group" text,
    "sub_group_id"      text,
    "save_path"         text,
    "torrent_name"      text,
    "name"              text,
    "url"               text,
    "homepage"          text,
    "downloaded"        text,
    "pushed"            text,
    "status"            text,
    "deleted"           integer DEFAULT 0
);

INSERT INTO rss_item_new (id, torrent_code, episode_num, rss_manage_id, translation_group, sub_group_id, save_path,
                          torrent_name, name, url, homepage, downloaded, pushed, deleted, status)
SELECT id,
       torrent_code,
       episode_num,
       rss_manage_id,
       translation_group,
       sub_group_id,
       save_path,
       torrent_name,
       name,
       url,
       homepage,
       downloaded,
       pushed,
       deleted,
       status
FROM rss_item;

DROP TABLE rss_item;

ALTER TABLE rss_item_new
    RENAME TO rss_item;


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
       official_title_en,
       official_title_jp,
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
