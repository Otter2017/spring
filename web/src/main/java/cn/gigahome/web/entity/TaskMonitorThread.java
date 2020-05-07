package cn.gigahome.web.entity;

import java.util.List;

public class TaskMonitorThread extends Thread {
    private final List<TaskFuturePair> taskFuturePairs;

    TaskMonitorThread(List<TaskFuturePair> taskFuturePairs) {
        this.taskFuturePairs = taskFuturePairs;
        super.setDaemon(true);
    }

    @Override
    public void run() {
        while (true) {
            if (taskFuturePairs != null) {
                synchronized (taskFuturePairs) {
                    if (taskFuturePairs.size() > 0) {
                        for (TaskFuturePair taskFuturePair : taskFuturePairs) {
                            if (taskFuturePair.isTaskExecuted()) {
                                taskFuturePair.getScheduledFuture().cancel(true);
                                taskFuturePairs.remove(taskFuturePair);
                            }
                        }
                    }
                }
            }
        }
    }
}