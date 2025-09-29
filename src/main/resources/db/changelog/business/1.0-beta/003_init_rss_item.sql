CREATE TABLE "rss_item"
(
    "id"                integer NOT NULL COLLATE BINARY PRIMARY KEY AUTOINCREMENT,
    "torrent_code"      text,
    "episode_num" integer DEFAULT 0,
    "rss_manage_id"     integer,
    "translation_group" text,
    "sub_group_id" text,
    "save_path" text,
    "torrent_name"      text,
    "name"              text,
    "url"               text,
    "homepage"          text,
    "downloaded"        text,
    "pushed"            text,
    "deleted"           integer DEFAULT 0
);