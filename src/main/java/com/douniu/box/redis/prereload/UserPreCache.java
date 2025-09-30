package com.douniu.box.redis.prereload;
import com.douniu.box.redis.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPreCache extends AbstractPreCache {

    private final RedisUtils redisUtils;

    private final String USER_CACHE_KEY = "user_info";

    @Override
    protected void init() {
        // 加载用户缓存
        if (!redisUtils.exist(USER_CACHE_KEY)) {
            redisUtils.set(USER_CACHE_KEY, "user1", 30 * 60);
        }
    }

    @Override
    protected void clear() {
        // 清空用户缓存
        redisUtils.delete(USER_CACHE_KEY);
    }

    @Override
    protected <T> T get() {
        // 获取用户缓存
        if (!redisUtils.exist(USER_CACHE_KEY)) {
            reload();
        }
        return (T) redisUtils.get(USER_CACHE_KEY);
    }
}
