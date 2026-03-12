package com.douniu.box.core.base;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class NettyTimeWheelDemo {

    private static final OrderService orderService = new OrderService();
    private static final HashedWheelTimer timer;

    static {
        // 1. 创建 ThreadFactory (用于给时间轮的线程命名，方便排查问题)
        ThreadFactory threadFactory = Executors.defaultThreadFactory();

        // 2. 创建 HashedWheelTimer
        // 参数说明：
        // threadFactory: 线程工厂
        // 100, TimeUnit.MILLISECONDS: 每一格的时间间隔 (tickDuration)
        // 512: 时间轮一圈有多少格 (ticksPerWheel)，建议设为 2 的 N 次方
        timer = new HashedWheelTimer(
                threadFactory, 
                100, 
                TimeUnit.MILLISECONDS, 
                512
        );
        System.out.println("时间轮已启动: " + LocalDateTime.now());
    }

    /**
     * 提交订单1小时后自动好评任务
     */
    public static Timeout submitAutoCommentTask(String orderId) {
        // 创建自动好评任务
        TimerTask autoCommentTask = new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                orderService.autoCommentOrder(orderId);
            }
        };

        // 将任务加入时间轮，延迟 1 小时执行
        return timer.newTimeout(autoCommentTask, 1, TimeUnit.HOURS);
    }

    public static void main(String[] args) throws InterruptedException {
        // 模拟创建3个订单
        Order order1 = orderService.createOrder("ORDER_001", "USER_001", 100.0);
        Order order2 = orderService.createOrder("ORDER_002", "USER_002", 200.0);
        Order order3 = orderService.createOrder("ORDER_003", "USER_003", 300.0);

        // 为每个订单提交1小时后自动好评任务
        submitAutoCommentTask(order1.getOrderId());
        submitAutoCommentTask(order2.getOrderId());
        submitAutoCommentTask(order3.getOrderId());

        System.out.println("所有订单已创建并提交自动好评任务: " + LocalDateTime.now());
        System.out.println("订单1状态: " + order1.getStatus() + ", 评价状态: " + order1.getCommentStatus());
        System.out.println("订单2状态: " + order2.getStatus() + ", 评价状态: " + order2.getCommentStatus());
        System.out.println("订单3状态: " + order3.getStatus() + ", 评价状态: " + order3.getCommentStatus());

        // 为了演示，我们将等待时间缩短为10秒
        System.out.println("\n等待10秒后查看自动好评结果...");
        Thread.sleep(10000);

        // 检查订单评价状态
        System.out.println("\n10秒后订单状态:");
        System.out.println("订单1状态: " + orderService.getOrder("ORDER_001").getStatus() + ", 评价状态: " + orderService.getOrder("ORDER_001").getCommentStatus());
        System.out.println("订单2状态: " + orderService.getOrder("ORDER_002").getStatus() + ", 评价状态: " + orderService.getOrder("ORDER_002").getCommentStatus());
        System.out.println("订单3状态: " + orderService.getOrder("ORDER_003").getStatus() + ", 评价状态: " + orderService.getOrder("ORDER_003").getCommentStatus());

        // 优雅关闭
        timer.stop(); 
        System.out.println("\n时间轮已停止");
    }
}