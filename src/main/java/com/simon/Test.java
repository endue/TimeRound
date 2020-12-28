package com.simon;

/**
 * @author Simon
 * @version 1.0.0
 * @Description 测试
 * @createTime 2020年12月25日 17:35:00
 */
public class Test {

    public static void main(String[] args) throws InterruptedException {
        SystemTimer systemTimer = SystemTimer.getInstance();

        TimerTaskEntry taskEntry1 = new TimerTaskEntry(1000L, () -> System.out.println("任务执行1->" + System.currentTimeMillis()));
        TimerTaskEntry taskEntry2 = new TimerTaskEntry(2000L, () -> System.out.println("任务执行2->" + System.currentTimeMillis()));
        TimerTaskEntry taskEntry3 = new TimerTaskEntry(3000L, () -> System.out.println("任务执行3->" + System.currentTimeMillis()));
        TimerTaskEntry taskEntry4 = new TimerTaskEntry(4000L, () -> System.out.println("任务执行4->" + System.currentTimeMillis()));
        TimerTaskEntry taskEntry5 = new TimerTaskEntry(5000L, () -> System.out.println("任务执行5->" + System.currentTimeMillis()));
        TimerTaskEntry taskEntry6 = new TimerTaskEntry(6000L, () -> System.out.println("任务执行6->" + System.currentTimeMillis()));
        TimerTaskEntry taskEntry7 = new TimerTaskEntry(10000L, () -> System.out.println("任务执行7->" + System.currentTimeMillis()));
        TimerTaskEntry taskEntry8 = new TimerTaskEntry(20000L, () -> System.out.println("任务执行8->" + System.currentTimeMillis()));


        systemTimer.addTask(taskEntry1);
        systemTimer.addTask(taskEntry2);
        systemTimer.addTask(taskEntry3);
        systemTimer.addTask(taskEntry4);
        systemTimer.addTask(taskEntry5);
        systemTimer.addTask(taskEntry6);
        systemTimer.addTask(taskEntry7);
        systemTimer.addTask(taskEntry8);

        Thread.sleep(20000);

        new Thread(() -> {
            TimerTaskEntry taskEntry9 = new TimerTaskEntry(1500L, () -> System.out.println("任务执行9->" + System.currentTimeMillis()));
            systemTimer.addTask(taskEntry9);
        }).start();
    }
}
