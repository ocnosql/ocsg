package com.ai.ocsg.process.cache;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by wangkai8 on 16/12/7.
 */
public class CacheQueue {

    private static BlockingQueue<CacheExecutor> executorQueue = new ArrayBlockingQueue<CacheExecutor>(10000);
    private static CacheQueue queue;
    private int handlerNum = 1;

    private CacheQueue() {
        for(int i = 0; i < handlerNum; i ++) {
            new CacheHandler().start();
        }
    }


    public static CacheQueue getInstance() {
        if(queue == null) {
            synchronized (CacheQueue.class) {
                if(queue == null) {
                    queue = new CacheQueue();
                }
            }
        }
        return queue;
    }


    public boolean addExecuter(CacheExecutor executor) {
        return executorQueue.offer(executor);
    }


    class CacheHandler extends Thread {

        public void run() {
            while(true) {
                try {
                    CacheExecutor cacheExecutor = executorQueue.take();
                    cacheExecutor.execute();
                } catch (InterruptedException e) {

                }
            }
        }
    }
}
