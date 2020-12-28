package com.simon;

import java.util.UUID;
import java.util.concurrent.DelayQueue;

/**
 * @author Simon
 * @version 1.0.0
 * @Description 时间轮
 * @createTime 2020年12月25日 16:12:00
 */
public class TimingWheel {
    // 一个槽表示的时间范围
    private Long tickMs;
    // 轮大小
    private Integer wheelSize;
    // 一个时间轮表示的时间范围
    private Long interval;
    // 时间轮指针
    private volatile long currentTime;
    private TimerTaskList[] buckets;
    private DelayQueue<TimerTaskList> delayQueue;
    // 上层时间轮
    private volatile TimingWheel overflowWheel;

    public TimingWheel(Long tickMs, Integer wheelSize, Long currentTime, DelayQueue<TimerTaskList> delayQueue) {
        this.tickMs = tickMs;
        this.wheelSize = wheelSize;
        this.interval = tickMs * wheelSize;
        this.buckets = new TimerTaskList[wheelSize];
        this.currentTime = currentTime - (currentTime % tickMs);
        this.delayQueue = delayQueue;
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = new TimerTaskList(UUID.randomUUID().toString());
        }
    }

    /**
     * 添加任务到对应的槽
     * @param timedTask
     * @return
     */
    public boolean addTask(TimerTaskEntry timedTask) {
        Long expirationMs = timedTask.getExpirationMs();
        if(timedTask.isCancle()){// 任务取消
            return false;
        }
        long delayMs = expirationMs - currentTime;
        if(delayMs < tickMs){// 任务到期
            return false;
        }else if(delayMs < interval){// 添加到对应的槽
            long virtualId = expirationMs / tickMs;
            int bucketIndex = (int) (virtualId % wheelSize);
            TimerTaskList bucket = buckets[bucketIndex];
            bucket.addTask(timedTask);
            // 设置槽过期时间并将任务入队
            if(bucket.setExpiration(expirationMs - (expirationMs % tickMs))){
                delayQueue.offer(bucket);
            }
            return true;
        }else{// 添加到上层时间轮
            if(overflowWheel == null){
                addOverflowWheel();
            }
            overflowWheel.addTask(timedTask);
            return true;
        }
    }

    /**
     * 尝试推荐时间轮
     * @param expiration
     */
    public void advanceClock(Long expiration) {
        if(expiration >= currentTime + tickMs){
            currentTime = expiration - (expiration % tickMs);
            if(overflowWheel != null){
                overflowWheel.advanceClock(expiration);
            }
        }
    }

    private TimingWheel addOverflowWheel() {
        if (overflowWheel == null) {
            synchronized (this) {
                if (overflowWheel == null) {
                    // 注意这里第一个参数为interval
                    overflowWheel = new TimingWheel(interval, wheelSize, currentTime, delayQueue);
                }
            }
        }
        return overflowWheel;
    }
}
