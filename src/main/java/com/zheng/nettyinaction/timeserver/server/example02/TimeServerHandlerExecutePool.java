package com.zheng.nettyinaction.timeserver.server.example02;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author zhenglian
 * @Date 2019/6/20
 */
public class TimeServerHandlerExecutePool {
    
    private ExecutorService executor;
    
    public TimeServerHandlerExecutePool(int maxPoolSize, int queueSize) {
        executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), maxPoolSize, 120L, 
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(queueSize), 
                new BasicThreadFactory.Builder().daemon(false).namingPattern("TimeServerThread-%d").build());
    }
    
    public void execute(Runnable runnable) {
        executor.execute(runnable);
    }
}

