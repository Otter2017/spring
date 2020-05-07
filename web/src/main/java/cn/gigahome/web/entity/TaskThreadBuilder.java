package cn.gigahome.web.entity;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskThreadBuilder implements ThreadFactory {
    private String threadNamePrefix;

    private boolean isDaemon;

    private AtomicInteger threadIndex;

    public TaskThreadBuilder(String namePrefix, boolean isDaemon) {
        this.threadNamePrefix = namePrefix;
        this.isDaemon = isDaemon;
        threadIndex = new AtomicInteger(0);
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread newThread = new Thread(r);
        newThread.setDaemon(isDaemon);
        newThread.setName(threadNamePrefix + threadIndex.incrementAndGet());
        return newThread;
    }
}
