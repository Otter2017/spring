package cn.gigahome.web.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RepeatableScheduleTask implements Runnable {
    private Logger logger = LoggerFactory.getLogger(RepeatableScheduleTask.class);

    private int delay;

    private TimeUnit timeUnit;

    private final int repeatTimes;

    private AtomicInteger repeatIndex = new AtomicInteger(0);

    private AtomicInteger index = new AtomicInteger(0);

    public int getDelay() {
        return delay;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public RepeatableScheduleTask(int delay, TimeUnit timeUnit) {
        this.delay = delay;
        this.timeUnit = timeUnit;
        this.repeatTimes = 0;
    }

    public RepeatableScheduleTask(int delay, TimeUnit timeUnit, int repeatTimes) {
        this.delay = delay;
        this.timeUnit = timeUnit;
        this.repeatTimes = repeatTimes;
    }

    @Override
    public void run() {
        if (repeatTimes == 0 || (repeatTimes > 0 && repeatIndex.getAndIncrement() < repeatTimes)) {
            logger.info("delay = {},index = {}", delay, index.incrementAndGet());
        }
    }

    boolean executed() {
        return repeatTimes > 0 && repeatIndex.get() >= repeatTimes;
    }
}
