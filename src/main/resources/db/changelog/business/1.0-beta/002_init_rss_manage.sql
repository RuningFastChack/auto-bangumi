CREATE TABLE "rss_manage"
(
    "id"               integer NOT NULL COLLATE BINARY PRIMARY KEY AUTOINCREMENT,
    "official_title"   text,
    "season"           integer DEFAULT 0,
    "last_episode_num" integer DEFAULT 0,
    "status"           text,
    "filter"           text,
    "poster_link"      text,
    "save_path"        text,
    "deleted"          integer DEFAULT 0,
    "complete"         text,
    "update_week"      integer,
    "send_date"        text,
    "rss_list"         text
);