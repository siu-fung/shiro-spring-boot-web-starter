package me.sdevil507.supports.shiro.cache;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * redis缓存实现(利用hash操作)
 *
 * @author sdevil507
 */
public class ShiroRedisCache<K, V> implements Cache<K, V> {

    /**
     * 缓存名称
     */
    private String cacheName;

    /**
     * redis操作模板
     */
    private RedisTemplate<String, Object> redisTemplate;

    ShiroRedisCache(String cacheName, RedisTemplate<String, Object> redisTemplate) {
        this.cacheName = cacheName;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public V get(K key) throws CacheException {
        BoundHashOperations<String, K, V> hash = redisTemplate.boundHashOps(cacheName);
        Object k = hashKey(key);
        return hash.get(k);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V put(K key, V value) throws CacheException {
        BoundHashOperations<String, K, V> hash = redisTemplate.boundHashOps(cacheName);
        Object k = hashKey(key);
        hash.put((K) k, value);
        return value;
    }

    @Override
    public V remove(K key) throws CacheException {
        BoundHashOperations<String, K, V> hash = redisTemplate.boundHashOps(cacheName);
        Object k = hashKey(key);
        V value = hash.get(k);
        hash.delete(k);
        return value;
    }

    @Override
    public void clear() throws CacheException {
        redisTemplate.delete(cacheName);
    }

    @Override
    public int size() {
        BoundHashOperations<String, K, V> hash = redisTemplate.boundHashOps(cacheName);
        return Objects.requireNonNull(hash.size()).intValue();
    }

    @Override
    public Set<K> keys() {
        BoundHashOperations<String, K, V> hash = redisTemplate.boundHashOps(cacheName);
        return hash.keys();
    }

    @Override
    public Collection<V> values() {
        BoundHashOperations<String, K, V> hash = redisTemplate.boundHashOps(cacheName);
        return hash.values();
    }

    /**
     * 生成hash中key值
     * <p>
     * 当获取身份认证信息时，传入的key是String类型，携带的是唯一身份ID，所以直接返回</br>
     * 当获取身份授权信息时，传入的key为PrincipalCollection类型，是对象类型</br>
     * 如果直接返回对象类型作为hash中key，会导致无法检索到该值，无法指定删除</br>
     * 因此需要将PrincipalCollection类型key执行转换，并获取唯一身份ID，并返回作为hash中key
     *
     * @param key key值
     * @return 返回key值
     */
    private Object hashKey(K key) {
        // 判断是否是PrincipalCollection类型，如果是执行转换
        if (key instanceof PrincipalCollection) {
            PrincipalCollection pc = (PrincipalCollection) key;
            return pc.getPrimaryPrincipal().toString();
        }
        return key;
    }
}
