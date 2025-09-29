UPDATE "user"
SET "config" = '{"downLoadSetting":{"password":"adminadmin","savePath":"/","ssl":false,"url":"http://127.0.0.1","username":"admin","utilEnum":"QB"},"filterSetting":{"enable":true,"filterReg":["720",' ||
               '"\\d+-\\d","MKV","繁","CHT"]},"generalSetting":{"rssTimeOut":3600,"enable":true}}'
WHERE "id" = 1;
