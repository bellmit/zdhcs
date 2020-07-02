package net.northking.atp.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis工具类
 */
@Component
public class RedisUtil {

    @SuppressWarnings("rawtypes")
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 批量删除对应的value
     *
     * @param keys
     */
    public void remove(final String... keys) {
        for (String key : keys) {
            remove(key);
        }
    }

    /**
     * 批量删除key
     *
     * @param pattern
     */
    @SuppressWarnings("unchecked")
    public void removePattern(final String pattern) {
        Set<Serializable> keys = redisTemplate.keys(pattern);
        if (keys.size() > 0)
            redisTemplate.delete(keys);
    }

    /**
     * 删除对应的value
     *
     * @param key
     */
    @SuppressWarnings("unchecked")
    public void remove(final String key) {

        if (exists(key)) {
            redisTemplate.delete(key);
        }
    }

    /**
     * 判断缓存中是否有对应的value
     *
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean exists(final String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 读取缓存
     *
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public Object get(final String key) {
        Object result = null;
        ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
        result = operations.get(key);
        return result;
    }

    /**
     * 读取缓存
     *
     * @param token
     * @return
     */
    @SuppressWarnings("unchecked")
    public Object getByToken(final String token) {
        ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
        Object result = operations.get(token);
        return result;
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean set(final String key, Object value) {
        boolean result = false;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean set(final String key, Object value, Long expireTime) {
        boolean result = false;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 写入缓存_序列化
     *
     * @param key
     * @param value
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean set(final byte[] key,  Object value) {
        boolean result = false;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**设置key的过期时间
     *
     *
     */
    public void setExpire(String key ,Long expireTime){
        redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
    }


    /**
     * 获取所有符合条件key
     *
     * @param pattern
     */
//    @SuppressWarnings("unchecked")
    public Set<Serializable> keys(final String pattern) {
        Set<Serializable> keys = redisTemplate.keys(pattern);
        return keys;
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean setPlatform(final String key, Object value) {
        boolean result = false;
        try {
            JedisConnectionFactory jedisConnectionFactory = (JedisConnectionFactory)redisTemplate.getConnectionFactory();
            jedisConnectionFactory.setDatabase(1);
            redisTemplate.setConnectionFactory(jedisConnectionFactory);
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 还原
     * @return
     */
    @SuppressWarnings("unchecked")
    public void init() {
        JedisConnectionFactory jedisConnectionFactory = (JedisConnectionFactory)redisTemplate.getConnectionFactory();
        jedisConnectionFactory.setDatabase(0);
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
    }

    //序列化
    public byte[] serialize(Object object) {
        ObjectOutputStream oos = null;
        ByteArrayOutputStream baos = null;
        try {

            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            byte[] bytes = baos.toByteArray();
            return bytes;
        } catch (Exception e) {
            try {
                if(oos !=null) {
                    oos.close();
                }
                if(baos !=null) {
                    baos.close();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }
    //反序列化
    public static Object unserialize(byte[] bytes) {
        ByteArrayInputStream bais = null;
        try {

            bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (Exception e) {
            try {
                if(bais !=null) {
                    bais.close();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }
}
