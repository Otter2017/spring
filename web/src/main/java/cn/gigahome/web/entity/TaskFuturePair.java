package cn.gigahome.web.entity;

import java.util.concurrent.ScheduledFuture;

public class TaskFuturePair {
    private RepeatableScheduleTask repeatableScheduleTask;

    private ScheduledFuture scheduledFuture;

    ScheduledFuture getScheduledFuture() {
        return scheduledFuture;
    }

    TaskFuturePair(RepeatableScheduleTask repeatableScheduleTask, ScheduledFuture scheduledFuture) {
        this.repeatableScheduleTask = repeatableScheduleTask;
        this.scheduledFuture = scheduledFuture;
    }

    boolean isTaskExecuted() {
        return this.repeatableScheduleTask.executed();
    }
}