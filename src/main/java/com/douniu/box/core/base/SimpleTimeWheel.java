package com.douniu.box.core.base;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 简易时间轮实现 (Hashed Wheel Timer 简化版)
 */
public class SimpleTimeWheel {

    // 时间轮的槽（buckets），每个槽是一个链表
    private final List<TimeTask>[] wheel;
    // 时间轮每一格代表的时间跨度（毫秒）
    private final long tickDuration;
    // 时间轮的总格数
    private final int ticksPerWheel;
    // 当前指针指向的槽索引
    private volatile int currentTickIndex = 0;
    // 时间轮工作线程
    private Thread workerThread;
    // 任务执行线程池（防止任务执行阻塞时间轮指针）
    private final ExecutorService taskExecutor;
    // 运行状态
    private volatile boolean running = false;
    
    // 锁，用于保护槽的并发读写
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 构造函数
     *
     * @param tickDuration  每格的时间间隔 (例如 100ms)
     * @param unit          时间单位
     * @param ticksPerWheel 一圈有多少格 (例如 60格)
     */
    @SuppressWarnings("unchecked")
    public SimpleTimeWheel(long tickDuration, TimeUnit unit, int ticksPerWheel) {
        this.tickDuration = unit.toMillis(tickDuration);
        this.ticksPerWheel = ticksPerWheel;
        this.wheel = new LinkedList[ticksPerWheel];
        
        // 初始化每个槽
        for (int i = 0; i < ticksPerWheel; i++) {
            wheel[i] = new LinkedList<>();
        }
        
        // 使用单线程池来执行具体任务，避免卡住时间轮
        this.taskExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    /**
     * 添加任务
     */
    public void addTask(Runnable task, long delay, TimeUnit unit) {
        long delayMs = unit.toMillis(delay);
        
        // 1. 计算需要走多少格
        // 比如 tick=1s, delay=5.5s, 那么需要走 5格 (向下取整或四舍五入看需求，这里简单处理)
        long ticks = delayMs / tickDuration;

        // 2. 计算任务应该放在哪个槽 (index)
        // 目标槽 = (当前槽 + 偏移量) % 总槽数
        int targetIndex = (int) ((currentTickIndex + ticks) % ticksPerWheel);

        // 3. 计算圈数 (Round)
        // 如果 delay 超过了一圈的时间，需要记录圈数
        int rounds = (int) (ticks / ticksPerWheel);

        TimeTask timeTask = new TimeTask(task, rounds);

        // 加写锁放入槽中
        lock.writeLock().lock();
        try {
            wheel[targetIndex].add(timeTask);
            System.out.printf("任务加入: 延时%dms, 目标槽位:%d, 剩余圈数:%d%n", delayMs, targetIndex, rounds);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 启动时间轮
     */
    public void start() {
        if (running) return;
        running = true;
        workerThread = new Thread(new TickWorker(), "TimeWheel-Worker");
        workerThread.start();
        System.out.println("时间轮启动...");
    }

    /**
     * 停止时间轮
     */
    public void stop() {
        running = false;
        if (workerThread != null) {
            workerThread.interrupt();
        }
        taskExecutor.shutdown();
        System.out.println("时间轮停止.");
    }

    /**
     * 时间轮的核心工作线程（Ticker）
     */
    private class TickWorker implements Runnable {
        @Override
        public void run() {
            while (running) {
                long startTime = System.currentTimeMillis();

                // 1. 处理当前槽的任务
                processSlot(currentTickIndex);

                // 2. 指针向前移动
                currentTickIndex = (currentTickIndex + 1) % ticksPerWheel;

                // 3. 等待下一个 tick
                // 为了保持精度，需要计算处理任务消耗的时间
                long executionTime = System.currentTimeMillis() - startTime;
                long sleepTime = tickDuration - executionTime;
                
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }

    /**
     * 处理指定槽位中的任务
     */
    private void processSlot(int slotIndex) {
        lock.writeLock().lock(); // 使用写锁，因为可能涉及到移除元素
        try {
            List<TimeTask> slot = wheel[slotIndex];
            Iterator<TimeTask> iterator = slot.iterator();

            while (iterator.hasNext()) {
                TimeTask task = iterator.next();
                
                if (task.rounds > 0) {
                    // 如果圈数 > 0，说明还没到时间，圈数减 1
                    task.rounds--;
                } else {
                    // 圈数 == 0，说明到时间了
                    // 提交到线程池执行，不阻塞时间轮
                    taskExecutor.execute(task.runnable);
                    // 从槽中移除
                    iterator.remove();
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 内部任务包装类
     */
    private static class TimeTask {
        Runnable runnable; // 实际业务逻辑
        int rounds;        // 剩余圈数

        public TimeTask(Runnable runnable, int rounds) {
            this.runnable = runnable;
            this.rounds = rounds;
        }
    }

    // ================== 测试代码 ==================
    public static void main(String[] args) throws InterruptedException {
        // 创建一个时间轮：每格 1秒，一圈 10格 (总共10秒一圈)
        SimpleTimeWheel timeWheel = new SimpleTimeWheel(1, TimeUnit.SECONDS, 10);
        
        timeWheel.start();

        // 任务1：延时 2秒 (在当前圈执行)
        timeWheel.addTask(() -> System.out.println(">>> 任务1执行 (2s) " + System.currentTimeMillis()), 2, TimeUnit.SECONDS);

        // 任务2：延时 12秒 (1圈 + 2格)
        timeWheel.addTask(() -> System.out.println(">>> 任务2执行 (12s) " + System.currentTimeMillis()), 12, TimeUnit.SECONDS);

        // 任务3：延时 5秒
        timeWheel.addTask(() -> System.out.println(">>> 任务3执行 (5s) " + System.currentTimeMillis()), 5, TimeUnit.SECONDS);

        // 主线程等待足够长的时间以观察结果
        Thread.sleep(15000);
        timeWheel.stop();
    }
}