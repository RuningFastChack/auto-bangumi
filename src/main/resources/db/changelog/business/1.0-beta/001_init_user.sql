
CREATE TABLE "user"
(
    "id"       INTEGER NOT NULL COLLATE BINARY PRIMARY KEY AUTOINCREMENT,
    "username" text,
    "password" text,
    "config"   text
);
