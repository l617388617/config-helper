package com.github.wconfig.helper.component;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CommonThreadPool
 *
 * @author lupeng10
 * @create 2023-07-02 14:13
 */
public class CommonThreadPool {

    private static final AtomicInteger INTEGER = new AtomicInteger(0);

    public static final ThreadPoolExecutor pool = new ThreadPoolExecutor(4, 10, 30,
            TimeUnit.SECONDS, new LinkedBlockingQueue<>(20), r -> new Thread(r, "customer-" + INTEGER.incrementAndGet()) , new ThreadPoolExecutor.CallerRunsPolicy());

    public static final ScheduledThreadPoolExecutor schedulePool = new ScheduledThreadPoolExecutor(2);

}
