CREATE TABLE "user"
(
    "id"       INTEGER NOT NULL COLLATE BINARY PRIMARY KEY AUTOINCREMENT,
    "username" text,
    "password" text,
    "config"   text
);

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

CREATE TABLE "rss_item"
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
    "deleted"           integer DEFAULT 0
);

INSERT INTO "user" ("id", "username", "password", "config")
VALUES (1, 'admin', '$2a$10$NWxKrQEih1TMl/EtGB.UmO3lw6XUT6hgziNPhWD1vJwu4pNXY37uS',
        '{"downLoadSetting":{"password":"adminadmin","savePath":"/","ssl":false,"url":"http://127.0.0.1","username":"admin","utilEnum":"QB"},"filterSetting":{"enable":true,"filterReg":["720","\\d+-\\d","MKV","繁","CHT"]},"generalSetting":{"rssTimeOut":3600,"enable":true},"mcsManageSetting":{"mcsManageKey":"","daemonId":"","instanceId":""}}');
