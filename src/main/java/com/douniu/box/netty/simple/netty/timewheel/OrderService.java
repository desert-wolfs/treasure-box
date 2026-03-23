package com.douniu.box.netty.simple.netty.timewheel;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderService {
    // 模拟数据库存储订单
    private final ConcurrentHashMap<String, Order> orderMap = new ConcurrentHashMap<>();

    /**
     * 创建订单
     */
    public Order createOrder(String orderId, String userId, Double amount) {
        Order order = new Order(orderId, userId, amount);
        orderMap.put(orderId, order);
        System.out.println(LocalDateTime.now() + " - 创建订单: " + orderId + ", 用户: " + userId + ", 金额: " + amount);
        return order;
    }

    /**
     * 自动好评订单
     */
    public void autoCommentOrder(String orderId) {
        Order order = orderMap.get(orderId);
        if (order != null && "NOT_COMMENTED".equals(order.getCommentStatus())) {
            order.setCommentStatus("AUTO_COMMENTED");
            order.setCommentTime(LocalDateTime.now());
            System.out.println(LocalDateTime.now() + " - 订单自动好评成功: " + orderId);
        } else {
            System.out.println(LocalDateTime.now() + " - 订单自动好评失败: " + orderId + "，订单不存在或已评价");
        }
    }

    /**
     * 获取订单
     */
    public Order getOrder(String orderId) {
        return orderMap.get(orderId);
    }
}