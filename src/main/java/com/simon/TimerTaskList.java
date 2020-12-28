package com.simon;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author Simon
 * @version 1.0.0
 * @Description 槽(任务列表)
 * @createTime 2020年12月25日 16:12:00
 */
public class TimerTaskList implements Delayed {
    // 唯一标识
    private String id;
    // 槽过期时间
    private AtomicLong expiration = new AtomicLong(-1L);

    private TimerTaskEntry root = new TimerTaskEntry(-1L,null);

    {
        root.next = root;
        root.prev = root;
    }

    public TimerTaskList(String id) {
        this.id = id;
    }

    /**
     * 添加任务
     * @param timedTask
     */
    public void addTask(TimerTaskEntry timedTask) {
        synchronized (this) {
            timedTask.bucket = this;
            TimerTaskEntry tail = root.prev;

            timedTask.next = root;
            timedTask.prev = tail;

            tail.next = timedTask;
            root.prev = timedTask;
        }
    }

    /**
     * 删除任务
     * @param timedTask
     */
    public void removeTask(TimerTaskEntry timedTask) {
        synchronized (this) {
            if (timedTask.bucket.id.equals(this.id)) {
                timedTask.next.prev = timedTask.prev;
                timedTask.prev.next = timedTask.next;
                timedTask.bucket = null;
                timedTask.next = null;
                timedTask.prev = null;
            }
        }
    }

    /**
     * 重新分配槽
     * 执行当前槽任务时会调用该方法
     * @param timedTaskFlush
     */
    public void flush(Consumer<TimerTaskEntry> timedTaskFlush) {
        synchronized (this){
            TimerTaskEntry timedTask = root.next;
            while (timedTask != null && !timedTask.equals(root)){
                removeTask(timedTask);
                timedTaskFlush.accept(timedTask);
                timedTask = root.next;
            }
            expiration.set(-1);
        }
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return Math.max(0, unit.convert(expiration.get() - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
    }

    @Override
    public int compareTo(Delayed o) {
        if (o instanceof TimerTaskList) {
            return Long.compare(expiration.get(), ((TimerTaskList) o).expiration.get());
        }
        return 0;
    }

    public Boolean setExpiration(Long expiration) {
        return this.expiration.getAndSet(expiration) != expiration;
    }

    public Long getExpiration() {
        return expiration.get();
    }
}
