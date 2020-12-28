package com.simon;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Simon
 * @version 1.0.0
 * @Description 任务
 * @createTime 2020年12月25日 16:12:00
 */
public class TimerTaskEntry  {

    // 延迟时间
    private Long delayMs;
    // 任务
    private Runnable task;
    // 过期时间戳
    private Long expirationMs;
    private AtomicBoolean cancel;
    protected TimerTaskEntry next;
    protected TimerTaskEntry prev;
    protected TimerTaskList bucket;

    public TimerTaskEntry(Long delayMs, Runnable task) {
        this.delayMs = delayMs;
        this.task = task;
        this.expirationMs = System.currentTimeMillis() + delayMs;
        this.cancel = new AtomicBoolean(false);
        this.next = this.prev = null;
        this.bucket = null;
    }

    public boolean isCancle() {
        return cancel.get();
    }

    public boolean cancel(){
        return cancel.compareAndSet(false,true);
    }

    public Runnable getTask() {
        return task;
    }

    public Long getExpirationMs() {
        return expirationMs;
    }

}
