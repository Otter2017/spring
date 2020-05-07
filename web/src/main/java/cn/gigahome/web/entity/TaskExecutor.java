package cn.gigahome.web.entity;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TaskExecutor {
    private ScheduledExecutorService taskExecutor;

    private ExecutorService executorService;

    private final List<TaskFuturePair> taskFuturePairs = new ArrayList<>();

    public TaskExecutor() {
        taskExecutor = Executors.newScheduledThreadPool(2, new TaskThreadBuilder("TaskThread-", true));
        if (taskExecutor instanceof ScheduledThreadPoolExecutor) {
            ((ScheduledThreadPoolExecutor) taskExecutor).setRemoveOnCancelPolicy(true);
        }
        executorService = Executors.newSingleThreadExecutor();
    }

    public void execute(RepeatableScheduleTask task) {
        synchronized (taskFuturePairs) {
            ScheduledFuture future = taskExecutor.scheduleAtFixedRate(task, 0, task.getDelay(), task.getTimeUnit());
            taskFuturePairs.add(new TaskFuturePair(task, future));
        }
    }

    @PostConstruct
    public void init() {
        TaskMonitorThread taskMonitorThread = new TaskMonitorThread(this.taskFuturePairs);
        executorService.submit(taskMonitorThread);
    }
}