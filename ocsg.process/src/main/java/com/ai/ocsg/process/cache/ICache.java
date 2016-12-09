package com.ai.ocsg.process.cache;

import java.util.List;

/**
 * Created by wangkai8 on 16/12/7.
 */
public interface ICache {

    public void cacheValue(Object key, Object value, long timeout) throws CacheException;

    public Object getValue(Object key) throws CacheException;

}
