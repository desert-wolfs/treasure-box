package com.douniu.box.caffeine;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.github.benmanes.caffeine.cache.*;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * LocalCache
 * 本地缓存
 *
 */
public abstract class LocalCache<K, V> implements LoadingCache<K, V> {

    private static final Logger log = LoggerFactory.getLogger(LocalCache.class);
    private static final Executor CAFFEINE_EXECUTOR;

    static {
        ThreadFactory namedThreadFactory = ThreadFactoryBuilder.create()
                .setThreadFactory(Executors.defaultThreadFactory())
                .setNamePrefix("caffeine-")
                .build();

        // 默认不创建线程
        int processors = Runtime.getRuntime().availableProcessors();
        CAFFEINE_EXECUTOR = new ThreadPoolExecutor(2, processors * 2,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                namedThreadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private final LoadingCache<K, V> loadingCache;

    public LocalCache() {
        var maximumSize = maximumSize();
        var expireAfterWrite = expireAfterWrite();
        var expireAfterAccess = expireAfterAccess();
        var refreshAfterWrite = refreshAfterWrite();
        var caffeine = Caffeine.newBuilder();
        if (maximumSize != null) caffeine.maximumSize(maximumSize);
        if (expireAfterWrite != null) caffeine.expireAfterWrite(expireAfterWrite);
        if (expireAfterAccess != null) caffeine.expireAfterAccess(expireAfterAccess);
        if (refreshAfterWrite != null) caffeine.refreshAfterWrite(refreshAfterWrite);

        // 是否开启软&弱引用淘汰机制 (不可同时使用)
        if (weakValues()) caffeine.weakValues();
        if (softValues()) caffeine.softValues();

        // 是否开启过期刷新
        if (expiredRefresh()) {
            caffeine.scheduler(Scheduler.systemScheduler());
            caffeine.removalListener(this::onExpiredRefresh);
        }

        // 独立Caffeine线程池
        caffeine.executor(CAFFEINE_EXECUTOR);

        // 创建caffeine
        var localCacheLoader = new LocalCacheLoader<K, V>(this);
        this.loadingCache = caffeine.build(localCacheLoader);
        log.info("Initializing caffeine cache name: {}", getClass().getSimpleName());
    }

    /**
     * 最大大小
     *
     * @return boolean
     */
    protected Long maximumSize() {
        return null;
    }

    /**
     * 缓存有效时间（写入时间）
     * 默认未设置
     *
     * @return Duration
     */
    protected Duration expireAfterWrite() {
        return null;
    }

    /**
     * 缓存有效时间（访问间隔）
     * 默认未设置
     *
     * @return Duration
     */
    protected Duration expireAfterAccess() {
        return null;
    }

    /**
     * 自动刷新数据
     * 在指定时间内自动刷新缓存
     * 当发生对条目的第一个过时请求时，将执行自动刷新。
     * 触发刷新的请求将异步调用 CacheLoader.reload 并立即返回旧值。
     *
     * @return Duration
     */
    protected Duration refreshAfterWrite() {
        return null;
    }

    /**
     * 弱引用
     * 生命周期是下次gc的时候
     *
     * @return boolean
     */
    protected boolean weakValues() {
        return false;
    }

    /**
     * 软引用
     * 生命周期是GC时并且堆内存不够时触发清除
     *
     * @return boolean
     */
    protected boolean softValues() {
        return false;
    }


    /**
     * 是否开启过期刷新
     * 仅在 expireAfterWrite 和 expireAfterAccess 时生效
     *
     * @return boolean
     */
    protected boolean expiredRefresh() {
        return false;
    }

    /**
     * 自定义线程池
     * caffeine默认复用ForkJoinPool.commonPool()
     *
     * @see ForkJoinPool
     * @return Executor
     */
    protected Executor executor() {
        return null;
    }

    /**
     * 过期刷新事件
     *
     * @param key 键
     * @param value 值
     * @param cause 过期方式
     */
    private void onExpiredRefresh(K key, V value, RemovalCause cause) {
        if (cause == RemovalCause.EXPIRED) {
            loadingCache.refresh(key);
            log.debug("[Caffeine] Expired refresh by: {} key: {}", getClass().getSimpleName(), key);
        }
    }

    /**
     * 缓存数据加载
     *
     * @param key key
     * @return Value
     */
    protected abstract V load(K key);

    /**
     * 批量缓存数据加载
     * 建议重写为批量查询
     *
     * @param keys Iterable
     * @return Map<K, V>
     */
    protected Map<K, V> loadAll(Collection<K> keys) {
        var tempMap = new LinkedHashMap<K, V>();
        for (var key : keys) {
            var value = load(key);
            if (value == null) continue;

            tempMap.put(key, value);
        }
        return tempMap;
    }

    @Override
    public V get(K key) {
        if (key == null) {
            return null;
        }

        return loadingCache.get(key);
    }

    public V get(K key, V defaultValue) {
        V value = get(key);
        return value == null ? defaultValue : value;
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> iterable) {
        return loadingCache.getAll(iterable);
    }

    @Override
    public CompletableFuture<V> refresh(K key) {
        return loadingCache.refresh(key);
    }

    @Override
    public CompletableFuture<Map<K, V>> refreshAll(Iterable<? extends K> keys) {
        return loadingCache.refreshAll(keys);
    }

    @Override
    public @Nullable V getIfPresent(K key) {
        return loadingCache.getIfPresent(key);
    }

    @Override
    public @PolyNull V get(K key, Function<? super K, ? extends @PolyNull V> mappingFunction) {
        return loadingCache.get(key, mappingFunction);
    }

    @Override
    public Map<K, V> getAllPresent(Iterable<? extends K> keys) {
        return loadingCache.getAllPresent(keys);
    }

    @Override
    public void put(K k, V v) {
        loadingCache.put(k, v);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        loadingCache.putAll(map);
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys, Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction) {
        return loadingCache.getAll(keys, mappingFunction);
    }

    @Override
    public void invalidate(K key) {
        loadingCache.invalidate(key);
    }

    @Override
    public void invalidateAll(Iterable<? extends K> keys) {
        loadingCache.invalidateAll(keys);
    }

    @Override
    public void invalidateAll() {
        loadingCache.invalidateAll();
    }

    @Override
    public @NonNegative long estimatedSize() {
        return loadingCache.estimatedSize();
    }

    @Override
    public CacheStats stats() {
        return loadingCache.stats();
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        return loadingCache.asMap();
    }

    @Override
    public void cleanUp() {
        loadingCache.cleanUp();
    }

    @Override
    public Policy<K, V> policy() {
        return loadingCache.policy();
    }

    private record LocalCacheLoader<K, V>(LocalCache<K, V> localCache) implements CacheLoader<K, V> {

        @Override
        public @Nullable V load(K key) throws Exception {
            return localCache.load(key);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Map<? extends K, ? extends V> loadAll(Set<? extends K> keys) throws Exception {
            return localCache.loadAll((Collection<K>) keys);
        }
    }

}
