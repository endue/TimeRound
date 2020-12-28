package com.simon;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Simon
 * @version 1.0.0
 * @Description 延迟功能调度器
 * @createTime 2020年12月25日 16:01:00
 */
public class SystemTimer {
    // 执行任务线程池
    private ExecutorService taskExecutor = Executors.newFixedThreadPool(1, runnable -> {
        Thread thread = new Thread(runnable, "executor-pool");
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler((t, e) -> System.err.println("Uncaught exception in thread '" + t.getName() + "':" + e));
        return thread;
    });
    // 延迟队列
    private DelayQueue<TimerTaskList> delayQueue = new DelayQueue();
    // 时间轮
    private TimingWheel timingWheel = new TimingWheel(1000L, 5, System.currentTimeMillis(), delayQueue);

    private static SystemTimer INSTANCE;

    public static SystemTimer getInstance() {
        if (INSTANCE == null) {
            synchronized (SystemTimer.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SystemTimer();
                }
            }
        }
        return INSTANCE;
    }

    private SystemTimer() {
        new Thread(() -> {
            while (true){
                advanceClock(1000L);
            }
        }).start();
    }

    /**
     * 推动时间轮转动
     * @param timeoutMs
     */
    private void advanceClock(Long timeoutMs) {
        try {
            TimerTaskList bucket = delayQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
            if (bucket != null){
                timingWheel.advanceClock(bucket.getExpiration());
                bucket.flush(this::addTask);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 添加任务
     * @param timedTask
     */
    public void addTask(TimerTaskEntry timedTask) {
        if (!timingWheel.addTask(timedTask)) {// 到期或取消
            if (!timedTask.isCancle()) {// 到期立即执行
                taskExecutor.submit(timedTask.getTask());
            }
        }
    }
}
