package com.douniu.box.redis.prereload;

/**
 * 预加载缓存的抽象类，程序启动时加载热点缓存
 */
public abstract class AbstractPreCache {

    /**
     * 加载缓存
     */
    protected abstract void init();

    /**
     * 清空缓存
     */
    protected abstract void clear();

    /**
     * 获取缓存
     */
    protected abstract <T> T get();

     /**
      * 缓存命中次数
      */
    public void reload() {
        clear();
        init();
    }
}
