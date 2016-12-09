package com.ai.ocsg.process.cache;

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.TimeUnit;

/**
 * Created by wangkai8 on 16/12/7.
 */
public class RedisCache implements ICache {

    private static RedisCache cache;
    private RedisTemplate redisTemplate;
    private CacheQueue cacheQueue = CacheQueue.getInstance();

    private RedisCache() {
        cacheQueue = CacheQueue.getInstance();
        redisTemplate = new RedisTemplate();
        init(redisTemplate);
    }

    public static RedisCache getInstance() {
        if(cache == null) {
            synchronized (RedisCache.class) {
                if(cache == null) {
                    cache = new RedisCache();
                }
            }
        }
        return cache;
    }


    public void init(RedisTemplate redisTemplate) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxActive(20);
        jedisPoolConfig.setMaxWait(3000);
        jedisPoolConfig.setMaxIdle(10);
        jedisPoolConfig.setMinIdle(5);
        jedisPoolConfig.setTestOnBorrow(false);

        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName("localhost");
        factory.setPort(6379);
        factory.setPoolConfig(jedisPoolConfig);
        factory.afterPropertiesSet();

        redisTemplate.setConnectionFactory(factory);
        redisTemplate.afterPropertiesSet();
    }


    @Override
    public void cacheValue(final Object key, final Object value, final long timeout) throws CacheException {

        cacheQueue.addExecuter(new CacheExecutor() {
            public void execute() {
                redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
            }
        });
    }


    @Override
    public Object getValue(Object key) throws CacheException {
        return redisTemplate.opsForValue().get(key);
    }

}
