package com.douniu.box.redis;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * RedisService
 * Redis服务工具类
 *
 * @author Gaosx
 * @date 2017-07-17 21:12
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class RedisUtils {

    private final RedisTemplate<String, String> redisTemplate;
    private final ValueOperations<String, String> valueOperations;
    private final HashOperations<String, String, String> hashOperations;
    private final ListOperations<String, String> listOperations;
    private final SetOperations<String, String> setOperations;
    private final ZSetOperations<String, String> zSetOperations;
    private final HyperLogLogOperations<String, String> hyperLogLogOperations;
    private final Gson gson;

    /**
     * 默认过期时长，单位：秒
     */
    public final static long DEFAULT_EXPIRE = 60 * 60 * 24L;

    /**
     * 不设置过期时长
     */
    public final static long NOT_EXPIRE = -1;

    @Autowired
    RedisConnectionFactory redisConnectionFactory;

    public void sub(MessageListener listener, String key){
        
        if(Objects.isNull(listener) || StrUtil.isEmpty(key)){
            return;
        }
        redisConnectionFactory.getConnection().subscribe(listener,key.getBytes());
    }

    public void pub(String key,String message){
        if(StrUtil.isEmpty(message) || StrUtil.isEmpty(key)){
            return;
        }
        redisConnectionFactory.getConnection().publish(key.getBytes(),message.getBytes());
    }
    /**
     * JSON数据转成Object
     *
     * @param json JSON
     * @param clazz CLASS
     * @return 转换数据
     */
    @SuppressWarnings("unchecked")
    public <T> T fromJson(String json, Class<T> clazz) {
        if (json == null) return null;
        if (clazz.equals(String.class)) return (T) json;
        if (clazz.equals(Integer.class)) return (T) Integer.valueOf(json);
        if (clazz.equals(Long.class)) return (T) Long.valueOf(json);
        if (clazz.equals(Float.class)) return (T) Float.valueOf(json);
        if (clazz.equals(Double.class)) return (T) Double.valueOf(json);

        try {
            return gson.fromJson(json, clazz);
        } catch (Exception exception) {
            log.error("[redis]redis fromJson error", exception);
            return null;
        }
    }

    /**
     * JSON数据转成List
     *
     * @param jsonList JSON
     * @param tClass CLASS
     * @return 转换数据
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> fromJsonList(List<String> jsonList, Class<T> tClass) {
        if (CollUtil.isEmpty(jsonList)) return Collections.emptyList();
        if (Objects.equals(tClass, String.class)) return (List<T>) jsonList;

        try {
            List<T> tempList = new ArrayList<>(jsonList.size());
            for (String json : jsonList) {
                tempList.add(fromJson(json, tClass));
            }
            return tempList;
        } catch (Exception exception) {
            log.error("[redis]redis fromJson error", exception);
            return Collections.emptyList();
        }
    }

    /**
     * JSON数据转成Set
     *
     * @param jsonSet JSON
     * @param tClass CLASS
     * @return 转换数据
     */
    @SuppressWarnings("unchecked")
    public <T> Set<T> fromJsonSet(Set<String> jsonSet, Class<T> tClass) {
        if (CollUtil.isEmpty(jsonSet)) return Collections.emptySet();
        if (Objects.equals(tClass, String.class)) return (Set<T>) jsonSet;

        try {
            Set<T> tempList = new LinkedHashSet<>(jsonSet.size());
            for (String json : jsonSet) {
                tempList.add(fromJson(json, tClass));
            }
            return tempList;
        } catch (Exception exception) {
            log.error("[redis]redis fromJson error", exception);
            return Collections.emptySet();
        }
    }

    /**
     * Object转成JSON数据
     *
     * @param obj Object
     * @return JSONString
     */
    public String toJson(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer || obj instanceof Long
                || obj instanceof Float || obj instanceof Double
                || obj instanceof Boolean || obj instanceof String) {
            return String.valueOf(obj);
        }

        try {
            return gson.toJson(obj);
        } catch (Exception exception) {
            log.error("[redis]redis toJson error", exception);
            return null;
        }
    }

    public void set(String key, Object value, long expire) {
        if (expire != NOT_EXPIRE) {
            valueOperations.set(key, toJson(value), expire, TimeUnit.SECONDS);
        } else {
            valueOperations.set(key, toJson(value));
        }
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        valueOperations.set(key, toJson(value), timeout, unit);
    }

    public void expire(String key, long expire) {
        redisTemplate.expire(key, expire, TimeUnit.SECONDS);
    }

    public void expire(String key, long expire, TimeUnit unit) {
        redisTemplate.expire(key, expire, unit);
    }

    public RedisTemplate<String, String> getRedisTemplate() {
        return redisTemplate;
    }

    public boolean exist(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void expireAt(String key, final Date date) {
        redisTemplate.expireAt(key, date);
    }

    public void listSet(String key, String value, long expire) {
        listOperations.leftPush(key, value);
        if (expire != NOT_EXPIRE) {
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }
    }

    public void listSet(String key, String value) {
        listOperations.leftPush(key, value);
    }

    /**
     * 获取指定下标的数组元素
     *
     * @param key    list键
     * @param offset 索引位置
     */
    public String lget(String key, int offset) {
        return listOperations.index(key, offset);
    }

    /**
     * 获取指定下标的数组元素
     *
     * @param key    list键
     * @param offset 索引位置
     */
    public <T> T lget(String key, int offset, Class<T> tClass) {
        String data = lget(key, offset);
        return data == null ? null : fromJson(data, tClass);
    }

    /**
     * 查询区间范围内的元素
     *
     * @param key   list键
     * @param start 开始索引
     * @param end   结束索引
     */
    public List<String> lrange(String key, int start, int end) {
        return listOperations.range(key, start, end);
    }

    public <T> List<T> lrange(String key, int start, int end, Class<T> tClass) {
        List<String> lrange = lrange(key, start, end);
        return fromJsonList(lrange, tClass);
    }

    /**
     * list 全部
     * @param key list键
     */
    public <T> List<T> lAll(String key, Class<T> tClass) {
        return lrange(key, 0, -1, tClass);
    }

    public void lRemove(String key, long count, String value) {
        listOperations.remove(key, count, value);
    }

    /**
     * 获取list集合长度
     */
    public long lSize(String key) {
        Long size = listOperations.size(key);
        if (size == null) {
            return 0L;
        }
        return size;
    }

    public void remove(String key) {
        String s = listOperations.leftPop(key);
        log.debug(key + " 删除为 : " + s);
    }

    /**
     * 从左到右，获取第一个元素并删除第一个元素
     */
    public String lPop(String key) {
        return listOperations.leftPop(key);
    }

    /**
     *  从左到右，获取第一个元素并删除第一个元素
     */
    public <T> T lPop(String key, Class<T> tClass) {
        String data = lPop(key);
        return fromJson(data, tClass);
    }


    /**
     * 批量放入数组
     *
     * @param key key
     * @param values values
     * @return Long
     */
    public Long leftPush(String key, Object values) {
        return listOperations.leftPush(key, toJson(values));
    }

    /**
     * 批量放入数组
     */
    public Long leftPushAll(String key, String... values) {
        return listOperations.leftPushAll(key, values);
    }

    /**
     * 数组放入集合 头掺法
     */
    public Long leftPushAll(String key, Collection<?> values) {
        if (CollUtil.isEmpty(values)) return 0L;
        String[] strArr = values.stream()
                .map(this::toJson)
                .toArray(String[]::new);
        return listOperations.leftPushAll(key, strArr);
    }

    public Long rightPush(String key, Object values) {
        return listOperations.rightPush(key, toJson(values));
    }

    /**
     * 数组放入集合,尾插法
     */
    public Long rightPushAll(String key, Collection<?> values) {
        if (CollUtil.isEmpty(values)) return 0L;
        String[] strArr = values.stream()
                .map(this::toJson)
                .toArray(String[]::new);
        return listOperations.rightPushAll(key, strArr);
    }

    public void set(String key, Object value) {
        valueOperations.set(key, toJson(value));
    }

    public <T> T get(String key, Class<T> clazz, long expire) {
        String value = valueOperations.get(key);
        if (expire != NOT_EXPIRE) {
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);

        }
        return StrUtil.isEmpty(value) ? null : fromJson(value, clazz);
    }

    public <T> T get(String key, Class<T> clazz) {
        return get(key, clazz, NOT_EXPIRE);
    }

    public String get(String key, long expire) {
        String value = valueOperations.get(key);
        if (expire != NOT_EXPIRE) {
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }
        return value;
    }

    public String get(String key) {
        return get(key, NOT_EXPIRE);
    }

    public <T> List<T> getList(String key, Class<T> clazz) {
        String value = get(key);
        if (StrUtil.isEmpty(value)) return Collections.emptyList();

        TypeToken<?> parameterized = TypeToken.getParameterized(ArrayList.class, clazz);
        return gson.fromJson(value, parameterized.getType());
    }

    /**
     * 如果为空，返回null
     * @param key
     * @param clazz
     * @return
     * @param <T>
     */
    public <T> List<T> getListWithNull(String key, Class<T> clazz) {
        String value = get(key);
        if (StrUtil.isBlank(value)) return null;

        TypeToken<?> parameterized = TypeToken.getParameterized(ArrayList.class, clazz);
        return gson.fromJson(value, parameterized.getType());
    }

    /**
     * Redis 获取Gzip数据
     *
     * @param key KEY
     * @param clazz Class<T>
     * @return T
     * @param <T> <T>
     */
    public <T> T getGzip(String key, Class<T> clazz) {
        return redisTemplate.execute((RedisConnection connection) -> {
            byte[] bytes = connection.get(key.getBytes());
            return bytes != null
                    ? fromJson(CompressUtils.decompressGzip(bytes), clazz)
                    : null;
        }, true);
    }

    /**
     * 批量获取Gzip数据
     * key为 redisKey
     *
     * @param keyPrefix key前缀
     */
    public <K, T> Map<K, T> mgetGzip(String keyPrefix, Collection<K> keys, Class<T> tClass) {
        if (CollUtil.isEmpty(keys)) return Collections.emptyMap();
        return redisTemplate.execute((RedisConnection connection) -> {
            List<String> concatKeys = concatKeys(keyPrefix, keys);
            List<byte[]> mGet = connection.mGet(concatKeys.stream()
                    .map(String::getBytes)
                    .toArray(byte[][]::new));

            // 拼装数据
            if (CollUtil.isEmpty(mGet)) return Collections.emptyMap();
            int size = keys.size();
            List<K> keyList = new ArrayList<>(keys);
            Map<K, T> result = new LinkedHashMap<>(size);
            for (int i = 0; i < size; i++) {
                byte[] data = mGet.get(i);
                if (null != data) {
                    String decompressData = CompressUtils.decompressGzip(data);
                    result.put(keyList.get(i), fromJson(decompressData, tClass));
                }
            }
            return result;
        });
    }

    /**
     * Redis 保存Gzip数据
     *
     * @param key KEY
     * @param value 数据
     */
    public void setGzip(String key, Object value) {
        redisTemplate.execute((RedisConnection connection) -> {
            String json = toJson(value);
            byte[] compress = CompressUtils.compressGzip(json);
            return compress != null
                    ? connection.set(key.getBytes(), compress)
                    : Boolean.FALSE;
        }, true);
    }

    /**
     * 拼接key
     *
     * @param keyPrefix key前缀
     * @param keys key值
     * @return List<String>
     */
    public List<String> concatKeys(String keyPrefix, Collection<?> keys) {
        if (CollUtil.isEmpty(keys)) return Collections.emptyList();

        Stream<String> stream = keys.stream().map(String::valueOf);
        if (StrUtil.isNotEmpty(keyPrefix)) {
            stream = stream.map(keyPrefix::concat);
        }
        return stream.toList();
    }

    /**
     * 转换为Map集合
     *
     * @param keys keys
     * @param cacheData 缓存数据
     * @param tClass 类型
     * @return Map<K, V>
     */
    private <K, V> Map<K, V> toMultiMap(Collection<K> keys, List<String> cacheData, Class<V> tClass) {
        if (CollUtil.isEmpty(keys)) return Collections.emptyMap();

        int size = keys.size();
        List<K> keyList = new ArrayList<>(keys);
        Map<K, V> result = new LinkedHashMap<>(size);
        for (int i = 0; i < size; i++) {
            String data = cacheData.get(i);
            if (null != data) {
                result.put(keyList.get(i), fromJson(data, tClass));
            }
        }
        return result;
    }

    /**
     * 批量获取数据
     * key为 redisKey
     */
    public <K, T> Map<K, T> mget(Collection<K> keys, Class<T> tClass) {
        return mget(null, keys, tClass);
    }

    /**
     * 批量获取数据
     * key为 redisKey
     *
     * @param keyPrefix key前缀
     */
    public <K, T> Map<K, T> mget(String keyPrefix, Collection<K> keys, Class<T> tClass) {
        List<String> list = concatKeys(keyPrefix, keys);
        if (list.isEmpty()) {
            return Collections.emptyMap();
        } else if (list.size() > 100) {
            List<List<String>> partition = Lists.partition(list, 100);
            List<String> dataList = partition.parallelStream()
                    .map(valueOperations::multiGet)
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .toList();

            return toMultiMap(keys, dataList, tClass);
        } else {
            List<String> dataList = valueOperations.multiGet(list);
            return toMultiMap(keys, dataList, tClass);
        }
    }

    public <T, K> Map<K, List<T>> mgetList(String keyPrefix, Collection<K> keys, Class<T> tClass) {
        List<String> dataList = valueOperations.multiGet(concatKeys(keyPrefix, keys));
        if (CollUtil.isEmpty(dataList)) return Collections.emptyMap();

        // 拼装返回返回参数
        List<K> keyList = new ArrayList<>(keys);
        Map<K, List<T>> result = new LinkedHashMap<>();
        for (int i = 0; i < keyList.size(); i++) {
            String data = dataList.get(i);
            if (data == null) continue;

            result.put(keyList.get(i), JSON.parseArray(data, tClass));
        }
        return result;
    }

    public boolean delete(String key) {
        return Objects.equals(Boolean.TRUE, redisTemplate.delete(key));
    }

    public Long delete(String... keys) {
        return redisTemplate.delete(Arrays.asList(keys));
    }

    /**
     * 批量删除key
     */
    public void delete(Collection<String> keys) {
        if (CollUtil.isNotEmpty(keys)) {
            redisTemplate.delete(keys);
        }
    }

    public void delete(String keyPrefix, Collection<?> keys) {
        delete(concatKeys(keyPrefix, keys));
    }

    /**
     * 删除某个前缀打头的
     */
    public void deleteKeys(String key) {
        Set<String> keys = redisTemplate.keys(key + "*");
        redisTemplate.delete(keys);
    }

    /**
     * 在redis中做原子加或者减操作
     *
     * @param tabKey hash table在redis中的key
     * @param rowId  要修改的行ID
     * @param value  要增加的数量, 如果为null, 则默认为1
     * @return 原子加之后的结果
     */
    public int atomAddOrMinus(final String tabKey, final String rowId, final Integer value) {
        // 如果是减操作, 保证数据不为负数
        if (value == -1) {
            Integer cacheDate = hget(tabKey, rowId, Integer.class);
            if (cacheDate == null || cacheDate == 0) {
                return 0;
            }
        }
        final Long result = hashOperations.increment(tabKey, rowId, value);
        return result.intValue();
    }

    public Long increment(String Key) {
        Objects.requireNonNull(Key, " key cannot be null");
        return valueOperations.increment(Key);
    }

    public Long increment(String Key, long increment) {
        Objects.requireNonNull(Key, " key cannot be null");
        return valueOperations.increment(Key, increment);
    }

    public Long decrement(String Key, long decrement) {
        Objects.requireNonNull(Key, " key cannot be null");
        return valueOperations.decrement(Key, decrement);
    }

    /**
     * hash的缓存操作
     *
     * @param tabKey key
     * @param rowId  field
     * @param value  值的大小 可能是钻石 魅力值 关注数 等等
     */
    public void hset(String tabKey, String rowId, String value) {
        hset(tabKey, rowId, value, NOT_EXPIRE);
    }

    /**
     * hash的缓存操作
     *
     * @param tabKey key
     * @param rowId  field
     * @param value  值的大小 可能是钻石 魅力值 关注数 等等
     */
    public void hset(String tabKey, String rowId, Object value, long expire) {
        hset(tabKey, rowId, toJson(value), expire);
    }

    /**
     * hash的缓存操作
     *
     * @param tabKey key
     * @param rowId  field
     * @param value  值的大小 可能是钻石 魅力值 关注数 等等
     */
    public void hset(String tabKey, String rowId, String value, long expire) {
        Objects.requireNonNull(tabKey, "hash table redis key cannot be null");
        Objects.requireNonNull(rowId, "row id in hash table cannot be null");
        Objects.requireNonNull(value, "value cannot be null");
        hashOperations.put(tabKey, rowId, value);
        if (expire != NOT_EXPIRE) {
            redisTemplate.expire(tabKey, expire, TimeUnit.SECONDS);
        }
    }

    /**
     * hash的缓存操作
     *
     * @param tabKey key
     */
    public void hset(String tabKey, Map<String, String> data) {
        if (CollUtil.isNotEmpty(data)) {
            hashOperations.putAll(tabKey, data);
        } else {
            log.error("redis map 放入 {} 失败 value 为空", tabKey);
        }
    }

    public boolean hsetIfAbsent(String key, Number hashKey, Object value) {
        return hsetIfAbsent(key, String.valueOf(hashKey), value);
    }

    public boolean hsetIfAbsent(String key, String hashKey, Object value) {
        return hsetIfAbsent(key, hashKey, toJson(value));
    }

    public boolean hsetIfAbsent(String key, String hashKey, String value) {
        return Boolean.TRUE.equals(hashOperations.putIfAbsent(key, hashKey, value));
    }

    public void hset(String tabKey, String rowId, Object value) {
        hset(tabKey, rowId, value, NOT_EXPIRE);
    }

    public void hset(String tabKey, Number rowId, Object value) {
        hset(tabKey, String.valueOf(rowId), value);
    }

    public String hget(String key, String field) {
        return hget(key, field, String.class);
    }

    public String hget(String key, Number field) {
        return hget(key, field, String.class);
    }

    public <T> T hget(String key, String field, Class<T> tClass) {
        String data = hashOperations.get(key, field);
        if (data == null) return null;

        return fromJson(data, tClass);
    }

    public <T> List<T> hgetList(String key, String field, Class<T> tClass) {
        String data = hashOperations.get(key, field);
        if (StrUtil.isEmpty(data)) return null;

        TypeToken<?> parameterized = TypeToken.getParameterized(ArrayList.class, tClass);
        return gson.fromJson(data, parameterized.getType());
    }

    public <T> T hget(String key, Number field, Class<T> tClass) {
        return field == null ? null : hget(key, field.toString(), tClass);
    }

    public void hsetAll(String tabKey, Map<?, ?> values) {
        if (CollUtil.isEmpty(values)) return;

        Map<String, String> cacheMap = new HashMap<>();
        for (Map.Entry<?, ?> item : values.entrySet()) {
            String key = toJson(item.getKey());
            if (key == null) continue;

            String value = toJson(item.getValue());
            cacheMap.put(key, value);
        }
        hashOperations.putAll(tabKey, cacheMap);
    }

    public boolean hsetnx(String key, String child, String value, long expire) {
        if (hsetIfAbsent(key, child, value)) {
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 从map 获取多个key
     *
     * @param key    hash key
     * @param fields 字段
     * @param tClass 返回类型
     * @return Map<String, T>
     */
    public <K, T> Map<K, T> hMultiGet(String key, Collection<K> fields, Class<T> tClass) {
        List<String> list = hashOperations.multiGet(key, concatKeys(null, fields));
        return toMultiMap(fields, list, tClass);
    }

    /**
     * 从map 获取多个key
     *
     * @param key    hash key
     * @param fields 字段
     * @param tClass 返回类型
     * @return List<T>
     */
    public <T> List<T> hgets(String key, Collection<?> fields, Class<T> tClass) {
        List<String> multiGet = hashOperations.multiGet(key, concatKeys(null, fields));
        return fromJsonList(multiGet, tClass);
    }

    public boolean hexists(String key, String field) {
        Boolean hasKey = hashOperations.hasKey(key, field);
        return Objects.equals(Boolean.TRUE, hasKey);
    }

    public Set<String> hkeys(String key) {
        return hashOperations.keys(key);
    }

    public Map<String, String> hgetAll(String key) {
        return hashOperations.entries(key);
    }
    public <T> List<T> hgetAll(String key, Class<T> clazz) {
        List<T> list =new ArrayList<>();
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        if (CollUtil.isEmpty(entries)) {
            return null;
        }
        entries.forEach((k,v)-> {
            T t = gson.fromJson(v.toString(), clazz);
            list.add(t);
        });

        return list;
    }

    public <T> List<T> hgetAllList(String key, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        if (CollUtil.isEmpty(entries)) {
            return list;
        }
        entries.forEach((k, v) -> {
            T t = gson.fromJson(v.toString(), clazz);
            list.add(t);
        });

        return list;
    }

    public <T> Set<T> hkeys(String key, Class<T> tClass) {
        Set<String> jsonSet = hashOperations.keys(key);
        return fromJsonSet(jsonSet, tClass);
    }

    public List<String> hvals(String key) {
        return hashOperations.values(key);
    }

    public <T> List<T> hvals(String key, Class<T> tClass) {
        List<String> values = hashOperations.values(key);
        return fromJsonList(values, tClass);
    }

    public Long hdel(String key, Number field) {
        return hashOperations.delete(key, String.valueOf(field));
    }

    public Long hdel(String key, Collection<?> fields) {
        Object[] fieldArray = fields.stream().filter(Objects::nonNull).map(Objects::toString).toArray(Object[]::new);
        if (fieldArray.length == 0) {
            return 0L;
        }

        return hashOperations.delete(key, fieldArray);
    }

    public Long hdel(String key, Object... field) {
        return hashOperations.delete(key, field);
    }

    public Long hincrement(String key, Number field, long delta) {
        return hincrement(key, field.toString(), delta);
    }

    public Long hincrement(String key, String field, long delta) {
        return hashOperations.increment(key, field, delta);
    }

    public Long hincrementIfPresent(String key, String field, long delta) {
        if (exist(key)) {
            return hincrement(key, field, delta);
        }else {
            return null;
        }
    }

    public <T> List<T> hrandomKeys(String key, long count, Class<T> tClass) {
        List<String> randomKeys = hashOperations.randomKeys(key, count);
        return fromJsonList(randomKeys, tClass);
    }

    public Long sadd(String key, Collection<?> values) {
        if (CollUtil.isEmpty(values)) return 0L;
        String[] array = values.stream()
                .map(this::toJson)
                .toArray(String[]::new);

        return setOperations.add(key, array);
    }

    public Long sadd(String key, String... value) {
        return setOperations.add(key, value);
    }

    public Set<String> smembers(String keys) {
        return setOperations.members(keys);
    }

    public <T> Set<T> smembers(String keys, Class<T> tClass) {
        Set<String> members = setOperations.members(keys);
        return fromJsonSet(members, tClass);
    }

    public boolean sismember(String key, String value) {
        return Boolean.TRUE.equals(setOperations.isMember(key, value));
    }

    public Set<String> sRandomMembers(String key, int count) {
        return setOperations.distinctRandomMembers(key, count);
    }

    public Set<String> sintersect(String key, Collection<String> values) {
        return setOperations.intersect(key, values);
    }
    public Long sSize(String key) {
        return setOperations.size(key);
    }

    public <T> Set<T> sintersect(String key, Collection<String> values, Class<T> tClass) {
        if (CollUtil.isEmpty(values)) return Collections.emptySet();

        Set<String> intersect = setOperations.intersect(key, values);
        return fromJsonSet(intersect, tClass);
    }

    public Long srem(String key, Object... values) {
        return setOperations.remove(key, values);
    }

    public boolean setIfAbsent(String key, String value) {
        Boolean ifAbsent = valueOperations.setIfAbsent(key, value);
        return ifAbsent != null && ifAbsent;
    }

    public boolean setIfAbsent(String key, String value, long timeout, TimeUnit unit) {
        Boolean ifAbsent = valueOperations.setIfAbsent(key, value, timeout, unit);
        return ifAbsent != null && ifAbsent;
    }

    public Boolean zadd(String key, String value, double score) {
        return zSetOperations.add(key, value, score);
    }

    public Boolean zadd(String key, Number value, double score) {
        return zSetOperations.add(key, value.toString(), score);
    }

    /**
     * Zset添加操作
     *
     * @param key   键
     * @param value 值
     * @param score 排序值
     */
    public Boolean zadd(String key, Object value, double score) {
        return zSetOperations.add(key, toJson(value), score);
    }

    /**
     * Zset添加操作
     *
     * @param key   键
     * @param value 值
     * @param score 排序值
     */
    public Boolean zaddIfAbsent(String key, Number value, double score) {
        return zaddIfAbsent(key, value.toString(), score);
    }

    /**
     * Zset添加操作
     *
     * @param key   键
     * @param value 值
     * @param score 排序值
     */
    public Boolean zaddIfAbsent(String key, String value, double score) {
        return zSetOperations.addIfAbsent(key, value, score);
    }

    /**
     * 获取set集合大小
     *
     * @param key Key
     * @return size
     */
    public long zsize(String key) {
        Long size = zSetOperations.size(key);
        return size == null ? 0 : size;
    }

    /**
     * 获取set集合大小
     *
     * @param key Key
     * @return size
     */
    public long zcount(String key, double start, double end) {
        Long size = zSetOperations.count(key, start, end);
        return size == null ? 0 : size;
    }


    public void zaddBach(String key, Set<TypedTuple<String>> se) {
        zSetOperations.add(key, se);
    }

    /**
     * Zset添加操作增加排序值
     *
     * @param key   键
     * @param value 值
     * @param score 排序值
     */
    public Double zincrby(String key, String value, double score) {
        return zSetOperations.incrementScore(key, value, score);
    }

    /**
     * Zset操作, 按照分数递增(从小到大)的顺序返回指定索引start到stop之间的元素,参数WITHSCORES指定显示分数
     *
     * @param key   键
     * @param start 查询起始值
     * @param end   查询结束值
     */
    public Set<String> zrange(String key, long start, long end) {
        return zSetOperations.range(key, start, end);
    }

    /**
     * Zset操作, 按照分数递增(从小到大)的顺序返回指定索引start到stop之间的元素,参数WITHSCORES指定显示分数
     *
     * @param key   键
     * @param start 查询起始值
     * @param end   查询结束值
     */
    public <T> Set<T> zrange(String key, long start, long end, Class<T> tClass) {
        Set<String> range = zSetOperations.range(key, start, end);
        return fromJsonSet(range, tClass);
    }

    /**
     * Zset操作, 按照分数值递减(从大到小)顺序返回指定索引start到stop之间的元素,参数WITHSCORES指定显示分数
     *
     * @param key   键
     * @param start 查询起始值
     * @param end   查询结束值
     */
    public Set<String> zrevrange(String key, long start, long end) {
        return zSetOperations.reverseRange(key, start, end);
    }

    public Set<String> zrangeByScore(String key, double start, double end, long offset, long count) {
        return zSetOperations.rangeByScore(key, start, end, offset, count);
    }

    public Set<TypedTuple<String>> zrangeWithScores(String key, long start, long end) {
        return zSetOperations.rangeWithScores(key, start, end);
    }

    public Set<TypedTuple<String>> zrevrangeWithScores(String key, long start, long end) {
        return zSetOperations.reverseRangeWithScores(key, start, end);
    }

    public Set<TypedTuple<String>> reverseRangeByScoreWithScores(String key, double start, double end, long offset, long count) {
        return zSetOperations.reverseRangeByScoreWithScores(key, start, end, offset, count);
    }

    public TypedTuple<String> zfirst(String key) {
        Set<TypedTuple<String>> typedTuples = zrangeWithScores(key, 0, 1);
        return CollUtil.getFirst(typedTuples);
    }

    public TypedTuple<String> zlast(String key) {
        Set<TypedTuple<String>> typedTuples = zrevrangeWithScores(key, 0, 1);
        return CollUtil.getFirst(typedTuples);
    }

    /**
     * Zset操作, 按照索引从小到大的顺序返回指定索引start到stop之间的元素,参数WITHSCORES指定显示分数
     *
     * @param key    键
     * @param start  查询起始值
     * @param end    查询结束值
     * @param tClass 需要转换是json 类
     */
    public <T> Set<T> zrevrange(String key, long start, long end, Class<T> tClass) {
        Set<String> set = zSetOperations.reverseRange(key, start, end);
        return fromJsonSet(set, tClass);
    }

    /**
     * 查询集合中指定顺序的值和score
     * 0, -1 表示获取全部的集合内容
     */
    public Set<TypedTuple<String>> rangeWithScoreAll(String key) {
        return zSetOperations.reverseRangeWithScores(key, 0, -1);
    }

    public Set<TypedTuple<String>> rangeWithScore(String key, long start, long end) {
        return zSetOperations.reverseRangeWithScores(key, start, end);
    }


    /**
     * Zset获得集合中元素的数量
     *
     * @param key 键
     */
    public Long zcard(String key) {
        return zSetOperations.size(key);
    }

    /**
     * Zset操作, 删除一个或者多个元素，返回删除元素的个数
     *
     * @param key   键
     * @param value 要删除的元素的值
     */
    public Long zrem(String key, String... value) {
        if (ArrayUtils.isNotEmpty(value)) {
            return zSetOperations.remove(key, (Object[]) value);
        } else {
            return 0L;
        }
    }

    /**
     * Zset操作, 删除一个或者多个元素，返回删除元素的个数
     *
     * @param key   键
     * @param values 要删除的元素的值
     */
    public Long zrem(String key, Number... values) {
        String[] temp = Stream.of(values)
                .map(String::valueOf)
                .toArray(String[]::new);

        return zrem(key, temp);
    }

    /**
     * Zset操作, 删除一个或者多个元素，返回删除元素的个数
     *
     * @param key   键
     * @param values 要删除的元素的值
     */
    public Long zrem(String key, Collection<?> values) {
        String[] temp = values.stream()
                .map(this::toJson)
                .toArray(String[]::new);

        return zrem(key, temp);
    }

    /**
     * Zset操作
     *
     * @param key   键
     */
    public Long zremByScore(String key, double min, double max) {
        return zSetOperations.removeRangeByScore(key, min, max);
    }

    /**
     * Zset操作, 删除指定区间的值
     *
     * @param key 键
     * @param min 最小值
     * @param max 最大值
     */
    public Long zremRangeByScore(String key, double min, double max) {
        return zSetOperations.removeRangeByScore(key, min, max);
    }

    public Long zremrange(String key, long start, long end) {
        return zSetOperations.removeRange(key, start, end);
    }

    /**
     * zset操作, 根据元素值返回元素对应的索引位置
     *
     * @param key   键
     * @param value 值
     * @return 索引下标
     */
    public Long zrank(String key, String value) {
        return zSetOperations.rank(key, value);
    }

    /**
     * zset操作, 返回有序集合中分值介于min和max之间的所有成员，包括min和max在内，并按照分值从小到大的排序来返回他们
     *
     * @param key 键
     * @param min 最小值
     * @param max 最大值
     */
    public Set<String> zrangebyscore(String key, double min, double max) {
        return zSetOperations.rangeByScore(key, min, max);
    }

    public <T> Set<T> zrangebyscore(String key, double start, double end, long offset, long count, Class<T> tClass) {
        Set<String> rangeByScore = zSetOperations.rangeByScore(key, start, end, offset, count);
        return fromJsonSet(rangeByScore, tClass);
    }

    public Set<String> zrevrangebyscore(String key, double start, double end, long offset, long count) {
        return zSetOperations.reverseRangeByScore(key, start, end, offset, count);
    }

    public <T> Set<T> zrevrangebyscore(String key, double start, double end, long offset, long count, Class<T> tClass) {
        Set<String> rangeByScore = zSetOperations.reverseRangeByScore(key, start, end, offset, count);
        return fromJsonSet(rangeByScore, tClass);
    }

    public <T> Set<T> zrangebyscore(String key, double min, double max, Class<T> tClass) {
        Set<String> rangeByScore = zSetOperations.rangeByScore(key, min, max);
        return fromJsonSet(rangeByScore, tClass);
    }

    public void setExpire(String key, Long expire) {
        if (expire != NOT_EXPIRE) {
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }

    }


    public Double zScore(String key, Object videoId) {
        return zSetOperations.score(key, videoId.toString());
    }

    public <T> Map<T, Double> zScoreMap(String key, List<T> items) {
        if (CollUtil.isNotEmpty(items)) {
            Object[] keys = items.stream()
                    .map(String::valueOf)
                    .toArray(Object[]::new);

            List<Double> scores = zSetOperations.score(key, keys);
            if (CollUtil.isNotEmpty(scores)) {
                Map<T, Double> data = new HashMap<>();
                for (int i = 0, scoresSize = scores.size(); i < scoresSize; i++) {
                    Double score = scores.get(i);
                    if (score == null) continue;

                    T item = items.get(i);
                    data.put(item, score);
                }
                return data;
            }
        }
        return Collections.emptyMap();
    }

    public Long zrevrank(String key, Integer userId) {
        return zSetOperations.reverseRank(key, userId.toString());
    }

    /**
     * 统计ZSET Scores
     *
     * @param key 键
     * @param start 查询起始值
     * @param end 查询结束值
     * @return sum
     */
    public double zrangeSumWithScores(String key, long start, long end) {
        return sumTypedTupleScore(zrangeWithScores(key, start, end));
    }

    /**
     * 统计ZSET Scores
     *
     * @param typedTuples Set<TypedTuple<String>>
     * @return double
     */
    private double sumTypedTupleScore(Set<TypedTuple<String>> typedTuples) {
        if (CollUtil.isEmpty(typedTuples)) return 0;
        return typedTuples.stream()
                .map(TypedTuple::getScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    /**
     * 获取hash集合长度
     *
     * @param key hash键
     */
    public Long hlen(String key) {
        Long size = hashOperations.size(key);
        if (size == null) {
            return 0L;
        }
        return size;
    }

    public Long ttl(String key) {
        return redisTemplate.getExpire(key);
    }

    public long getHLLSize(String key) {
        return hyperLogLogOperations.size(key);
    }

    public long addHLLValue(String key, String value) {
        return hyperLogLogOperations.add(key, value);
    }



    /**
     * 模糊查询 Redis 中所有符合条件的 keys
     * @param pattern 模糊查询的模式，例如 "user:*"
     * @return 符合条件的 keys 集合
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     *
     * @return 返回表示**操作执行前**指定偏移量 offset 处的原始位值
     * 返回 true：表示在设置新值之前，该偏移位的值为 1（已设置）
     * 返回 false：表示在设置新值之前，该偏移位的值为 0（未设置）
     */
    public boolean setBit(String key, long offset, boolean value) {
        return Boolean.TRUE.equals(valueOperations.setBit(key, offset, value));
    }

    public boolean getBit(String key, long offset) {
        return Boolean.TRUE.equals(valueOperations.getBit(key, offset));
    }

    /**
     * 统计bitMap中1的bit数量
     */
    public long bitCount(String key) {
        Long execute = redisTemplate.execute((RedisCallback<Long>) connection -> {
            RedisStringCommands redisStringCommands = connection.stringCommands();
            return redisStringCommands.bitCount(key.getBytes());
        });
        return execute == null ? 0 : execute;
    }

    /**
     * 查找第一个为指定值的bit位置
     * @param key 键
     * @param value 查找值(true=1/false=0)
     * @return 位置索引(-1表示未找到)
     */
    public Long bitPos(String key, boolean value) {
        return redisTemplate.execute((RedisCallback<Long>) connection ->
                connection.stringCommands().bitPos(key.getBytes(), value));
    }

    /**
     * 在指定范围内查找第一个为指定值的bit位置
     * @param key 键
     * @param value 查找值(true=1/false=0)
     * @param start 查找范围起始索引(包含) long有8个字节，对应的开始字节位置为 start * 8
     * @param end 查找范围结束索引(包含) long有8个字节，对应的结束字节位置为 (end + 1) * 8 - 1
     * 也就e查询字节范围为 [start * 8  -> (end + 1) * 8 - 1]
     * @return 位置索引(-1表示未找到)
     */
    public Long bitPos(String key, boolean value, long start, long end) {
        return redisTemplate.execute((RedisCallback<Long>) connection -> {
            Range<Long> range = Range.of(Range.Bound.inclusive(start), Range.Bound.inclusive(end));
            return connection.stringCommands().bitPos(key.getBytes(), value, range);
        });
    }
}
