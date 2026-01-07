package com.douniu.box.caffeine;

import cn.hutool.json.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Duration;

/**
 */
@Repository
@RequiredArgsConstructor
public class TestLocalCache extends LocalCache<Integer, Object> {


    @Override
    protected Duration refreshAfterWrite() {
        return Duration.ofMinutes(3);
    }

    @Override
    protected Object load(Integer giftId) {
        return new JSONObject();
    }


}
