package com.douniu.box.core.base;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Order implements Serializable {
    private String orderId;
    private String userId;
    private Double amount;
    private String status;
    private String commentStatus;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime commentTime;

    public Order() {}

    public Order(String orderId, String userId, Double amount) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.status = "PAID";
        this.commentStatus = "NOT_COMMENTED";
        this.createTime = LocalDateTime.now();
        this.payTime = LocalDateTime.now();
    }

    private static final long serialVersionUID = 1L;
}