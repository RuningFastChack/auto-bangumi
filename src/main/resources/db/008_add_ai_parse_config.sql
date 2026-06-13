-- 为已有用户配置添加 aiParseSetting 缺省值（仅当不存在时）
UPDATE "user"
SET config = json_set(
        config,
        '$.aiParseSetting',
        json('{"enabled":false,"provider":"DEEPSEEK","apiKey":"","model":"deepseek-v4-flash"}')
             )
WHERE json_extract(config, '$.aiParseSetting') IS NULL;
