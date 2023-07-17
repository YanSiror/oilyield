package com.example.demo.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RedisUtils {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public RedisUtils() {
    }

    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error(key, e);
            return false;
        }
    }

    public boolean checkCacheExists(String name) {
        Set<String> keys = redisTemplate.keys(name);
        return keys != null && !keys.isEmpty();
    }

    public void clearPageCache(String name) {
        Set<String> keys = redisTemplate.keys(name + '*');
        assert keys != null;
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // ============================String=============================
    /**
     * 普通缓存获取
     * @param key 键
     * @return 值
     */
    public Object get(String key, int id) {
        return key == null ? null : redisTemplate.opsForValue().get(key+ "::"+ id);
    }

    /**
     * 普通缓存获取多个
     * @param pattern 键约束
     * @return 值列表
     */
    public List<Object> multiGet(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        assert keys != null;
        return redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 普通缓存删除
     * @param key 键
     * @return 值
     */
    public Boolean delete(String key) {
        Boolean hasDel = redisTemplate.delete(key);
        return hasDel;
    }

    /**
     * 普通缓存放入
     * @param key  键
     * @param value  值
     * @return true成功 false失败
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error(key, e);
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key 键
     * @param value 值
     * @param time 时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error(key, e);
            return false;
        }
    }
}
