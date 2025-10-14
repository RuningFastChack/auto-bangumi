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
       '1'
FROM rss_item;

DROP TABLE rss_item;

ALTER TABLE rss_item_new
    RENAME TO rss_item;