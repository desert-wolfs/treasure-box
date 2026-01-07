package com.douniu.box.caffeine;

import cn.hutool.core.date.DateTime;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;


// 定义过期时间
@Slf4j
@Component
public class DefineExpireCache<T> {

    /**
     * @param expire 秒
     * @return
     */
    public Cache<String, T> createCache(long expire) {
        if (expire <= 0) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime featureTime = now.plusSeconds(expire);
        // 初始化 Caffeine 缓存，设置自定义过期策略, 今天过期
        return createBaseCache(now, featureTime);
    }

    public Cache<String, T> createTodayExpireCache() {
        // 初始化 Caffeine 缓存，设置自定义过期策略, 今天过期
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = now.with(LocalTime.MAX); // 23：59：59.999
        return createBaseCache(now, endOfDay);
    }


    private Cache<String, T> createBaseCache(LocalDateTime start, LocalDateTime end) {
        // 初始化 Caffeine 缓存，设置自定义过期策略, 今天过期
        return Caffeine.newBuilder()
                .expireAfter(new Expiry<String, T>() {
                    @Override
                    public long expireAfterCreate(String key, T value, long currentTime) {
                        // 计算当前时间到今天结束的剩余纳秒数
                        long between = ChronoUnit.NANOS.between(start, end);
                        log.info("[task]task, 当前任务已经完成, cacheKey:{}, 缓存过期时间:{}s", key, between / 1000000000);
                        return between;
                    }

                    @Override
                    public long expireAfterUpdate(String key, T value, long currentTime, long currentDuration) {
                        return currentDuration; // 更新时不改变过期时间
                    }

                    @Override
                    public long expireAfterRead(String key, T value, long currentTime, long currentDuration) {
                        return currentDuration; // 读取时不改变过期时间
                    }
                })
                .build();
    }

    public static void main(String[] args) {
        DefineExpireCache<Integer> cache = new DefineExpireCache<>();
        Cache<String, Integer> cacheVO = cache.createCache(1L);
        cacheVO.put("key", 3);
        Integer ifPresent = cacheVO.getIfPresent("key");
        log.info("ifPresent:{}", ifPresent);
        try {
            Thread.sleep(2000);
        } catch (Exception e) {

        }
        Integer ifPresent2 = cacheVO.getIfPresent("key");
        log.info("ifPresent2:{}", ifPresent2);
    }
}
