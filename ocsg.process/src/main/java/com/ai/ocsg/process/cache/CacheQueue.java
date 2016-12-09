package com.ai.ocsg.process.cache;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by wangkai8 on 16/12/7.
 */
public class CacheQueue {

    private static BlockingQueue queue = new ArrayBlockingQueue(10000);

    public static void addExecuter(CacheExecutor executor) {
        queue.offer(executor);
    }

    class
}
