package com.ai.ocsg.process.cache;

/**
 * Created by wangkai8 on 16/12/7.
 */
public class CacheException extends Exception {

    public CacheException() {

    }

    public CacheException(String msg) {
        super(msg);
    }

    public CacheException(Throwable throwable) {
        super(throwable);
    }

    public CacheException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
