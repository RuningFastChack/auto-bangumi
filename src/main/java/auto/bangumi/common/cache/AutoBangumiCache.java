package auto.bangumi.common.cache;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 缓存接口
 *
 * @author 查查
 * @since 2025/10/3
 */
public abstract class AutoBangumiCache {

    private static final TimedCache<String, Object> dataCatch = CacheUtil.newTimedCache(200);

    static {
        dataCatch.schedulePrune(20000);
    }

    public static <T> T getCache(String key, Class<T> clazz) {
        return clazz.cast(dataCatch.get(key));
    }

    public static <T> T getCache(String key, Class<T> clazz, Supplier<? extends T> elseGet, long time) {
        T cache = getCache(key, clazz);
        if (Objects.isNull(cache)) {
            cache = elseGet.get();
            setCache(key, cache, time);
        }
        return cache;
    }

    public static void setCache(String key, Object value, long time) {
        dataCatch.put(key, value, time);
    }

    public static void removeCache(String key) {
        dataCatch.remove(key);
    }
}
