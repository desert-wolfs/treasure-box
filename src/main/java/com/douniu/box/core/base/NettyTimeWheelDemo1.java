package com.douniu.box.core.base;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class NettyTimeWheelDemo1 {

    public static void main(String[] args) throws InterruptedException {

        // 1. 创建 ThreadFactory (用于给时间轮的线程命名，方便排查问题)
        ThreadFactory threadFactory = Executors.defaultThreadFactory();

        // 2. 创建 HashedWheelTimer
        // 参数说明：
        // threadFactory: 线程工厂
        // 100, TimeUnit.MILLISECONDS: 每一格的时间间隔 (tickDuration)
        // 512: 时间轮一圈有多少格 (ticksPerWheel)，建议设为 2 的 N 次方
        HashedWheelTimer timer = new HashedWheelTimer(
                threadFactory,
                100,
                TimeUnit.MILLISECONDS,
                512
        );

        System.out.println("时间轮已启动: " + LocalDateTime.now());

        // 3. 定义一个任务 (实现 TimerTask 接口)
        TimerTask task = new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                System.out.println("任务执行了: " + LocalDateTime.now());
                // 注意：这里不要写耗时逻辑！
            }
        };
        System.out.println("任务1111: " + LocalDateTime.now());


        // 4. 将任务加入时间轮，延迟 3 秒执行
        // 返回的 Timeout 对象可以用来取消任务
        Timeout timeout = timer.newTimeout(task, 3, TimeUnit.SECONDS);

        System.out.println("任务1112: " + LocalDateTime.now());

        // --- 演示取消任务 ---
        // 这里的任务设定为 5秒后执行
        Timeout cancelTask = timer.newTimeout(t -> {
            System.out.println("这条消息不应该打印，因为会被取消");
        }, 5, TimeUnit.SECONDS);

        System.out.println("任务1113: " + LocalDateTime.now());

        // 模拟业务逻辑，决定取消上面的任务
        Thread.sleep(1000);
        if (!cancelTask.isExpired()) {
            cancelTask.cancel(); // 取消任务
            System.out.println("5秒的任务已被取消");
        }

        System.out.println("任务1114: " + LocalDateTime.now());

        // 保持主线程运行以便观察结果
        Thread.sleep(5000);

        System.out.println("任务1115: " + LocalDateTime.now());

        // 5. 优雅关闭
        timer.stop();
        System.out.println("时间轮已停止");
    }
}