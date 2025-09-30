package com.douniu.box.mapper;

import com.douniu.box.mapper.entity.TestUser;

public interface TestUserDao {
    int deleteByPrimaryKey(Integer id);

    int insert(TestUser row);

    int insertSelective(TestUser row);

    TestUser selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TestUser row);

    int updateByPrimaryKey(TestUser row);
}