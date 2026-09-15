package com.wtls.blog_server.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 通用 Redis 常用工具类
 * 涵盖：键过期、字符串存取、计数器、哈希存取及标准分布式锁支持
 */
@Component
public class RedisUtils {

    private static final Logger log = LoggerFactory.getLogger(RedisUtils.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 分布式锁原子释放 Lua 脚本：只有当锁的 value 与传入的 requestId 一致时才执行 del
    private static final String UNLOCK_LUA_SCRIPT = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

    // ============================= Common 键管理 =============================

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间
     * @param unit 时间单位
     * @return 是否成功
     */
    public boolean expire(String key, long time, TimeUnit unit) {
        try {
            if (time > 0) {
                Boolean result = redisTemplate.expire(key, time, unit);
                return Boolean.TRUE.equals(result);
            }
            return false;
        } catch (Exception e) {
            log.error(">>> [Redis] 设置键 [{}] 过期时间失败: ", key, e);
            return false;
        }
    }

    /**
     * 获取缓存剩余过期时间（秒）
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效，返回-2代表键不存在
     */
    public long getExpire(String key) {
        try {
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return expire != null ? expire : -2;
        } catch (Exception e) {
            log.error(">>> [Redis] 获取键 [{}] 剩余过期时间失败: ", key, e);
            return -2;
        }
    }

    /**
     * 判断 key 是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            Boolean has = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(has);
        } catch (Exception e) {
            log.error(">>> [Redis] 判断键 [{}] 是否存在失败: ", key, e);
            return false;
        }
    }

    /**
     * 删除一个或多个缓存
     *
     * @param key 可以传一个值 或多个
     */
    public void del(String... key) {
        if (key != null && key.length > 0) {
            try {
                if (key.length == 1) {
                    redisTemplate.delete(key[0]);
                } else {
                    redisTemplate.delete(Arrays.asList(key));
                }
            } catch (Exception e) {
                log.error(">>> [Redis] 删除键失败: ", e);
            }
        }
    }

    // ============================ String 字符串操作 =============================

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        try {
            return key == null ? null : redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error(">>> [Redis] 获取键 [{}] 失败: ", key, e);
            return null;
        }
    }

    /**
     * 普通缓存获取并自动转换类型
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object obj = get(key);
        if (obj == null) {
            return null;
        }
        if (clazz.isInstance(obj)) {
            return (T) obj;
        }
        return null;
    }

    /**
     * 普通缓存放入（永久有效）
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error(">>> [Redis] 设置键 [{}] 失败: ", key, e);
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(String key, Object value, long time) {
        return set(key, value, time, TimeUnit.SECONDS);
    }

    /**
     * 普通缓存放入并设置带单位的时间
     */
    public boolean set(String key, Object value, long time, TimeUnit unit) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, unit);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error(">>> [Redis] 设置键 [{}] 失败: ", key, e);
            return false;
        }
    }

    /**
     * 递增计数
     *
     * @param key   键
     * @param delta 要增加几(大于0)
     */
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("递增因子必须大于0");
        }
        Long val = redisTemplate.opsForValue().increment(key, delta);
        return val != null ? val : 0;
    }

    /**
     * 递减计数
     *
     * @param key   键
     * @param delta 要减少几(大于0)
     */
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("递减因子必须大于0");
        }
        Long val = redisTemplate.opsForValue().decrement(key, delta);
        return val != null ? val : 0;
    }

    // ================================ Hash 哈希操作 =================================

    /**
     * HashGet
     */
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取hashKey对应的所有键值
     */
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * HashSet 并设置有效时间
     */
    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            log.error(">>> [Redis] 设置Hash [{}] 失败: ", key, e);
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     */
    public boolean hset(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            log.error(">>> [Redis] 设置Hash项 [{}:{}] 失败: ", key, item, e);
            return false;
        }
    }

    /**
     * 删除hash表中的值
     */
    public void hdel(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中是否有该项的值
     */
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    // ============================ 分布式锁支持 (Distribute Lock) =============================

    /**
     * 尝试获取分布式锁（开箱即用，支持防死锁自动超时释放）
     *
     * @param lockKey       锁的标识键
     * @param requestId     请求唯一标识（如 UUID 或业务标识，用于释放时防误删）
     * @param expireSeconds 锁持有最长时间（秒），避免因崩溃导致死锁
     * @return true 获取锁成功，false 获取锁失败
     */
    public boolean tryLock(String lockKey, String requestId, long expireSeconds) {
        try {
            Boolean success = redisTemplate.opsForValue().setIfAbsent(
                    lockKey,
                    requestId,
                    Duration.ofSeconds(expireSeconds)
            );
            return Boolean.TRUE.equals(success);
        } catch (Exception e) {
            log.error(">>> [RedisLock] 获取分布式锁 [{}] 异常: ", lockKey, e);
            return false;
        }
    }

    /**
     * 释放分布式锁（基于 Lua 脚本原子比对，保证只释放当前请求持有的锁）
     *
     * @param lockKey   锁的标识键
     * @param requestId 请求唯一标识
     * @return true 释放成功，false 锁已过期或不属于当前请求
     */
    public boolean releaseLock(String lockKey, String requestId) {
        try {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(UNLOCK_LUA_SCRIPT);
            redisScript.setResultType(Long.class);

            Long result = redisTemplate.execute(
                    redisScript,
                    Collections.singletonList(lockKey),
                    requestId
            );
            return Long.valueOf(1L).equals(result);
        } catch (Exception e) {
            log.error(">>> [RedisLock] 释放分布式锁 [{}] 异常: ", lockKey, e);
            return false;
        }
    }
}
