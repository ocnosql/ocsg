package com.ai.ocsg.process.cache;

/**
 * Created by wangkai8 on 16/12/7.
 */
public class CacheFactory {

    public static final String REDIS = "redis";

    public static final String MEMCACHED = "memcached";

    public static ICache getCache(String type) {
        if(REDIS.equals(type)) {
            return RedisCache.getInstance();
        }
        return null;
    }
}
