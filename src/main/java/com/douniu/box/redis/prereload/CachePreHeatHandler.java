package com.douniu.box.redis.prereload;

import com.douniu.box.utils.ApplicationContextUtil;
import org.springframework.boot.CommandLineRunner;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 缓存预加载处理类，程序启动时加载热点缓存
 * @ConditionalOnProperty这个注解在这里的作用是，需要在配置文件开启cache.init.enable，理想值是true，默认值是false。
 */
@Component
@ConditionalOnProperty(name = {"cache.init.enable"}, havingValue ="true", matchIfMissing = false)
public class CachePreHeatHandler implements CommandLineRunner {

    /**
     * 缓存预加载处理类，程序启动时加载热点缓存
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        ApplicationContext context = ApplicationContextUtil.getContext();
        Map<String, AbstractPreCache> beansOfType = context.getBeansOfType(AbstractPreCache.class);
        for (Map.Entry<String, AbstractPreCache> entry : beansOfType.entrySet()) {
            AbstractPreCache bean = context.getBean(entry.getValue().getClass());
            bean.init();
        }
    }
}
