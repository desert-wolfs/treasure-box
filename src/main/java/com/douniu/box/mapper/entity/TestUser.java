package com.douniu.box.mapper.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class TestUser implements Serializable {
    private Integer id;

    private String name;

    private Integer sex;

    private Date createTime;

    public TestUser() {}

    public TestUser(String name) {
        this.name = name;
    }

    private static final long serialVersionUID = 1L;
}