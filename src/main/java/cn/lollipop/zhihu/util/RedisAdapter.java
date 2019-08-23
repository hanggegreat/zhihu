package cn.lollipop.zhihu.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class RedisAdapter {
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public RedisAdapter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public Long sadd(String key, String value) {
        return stringRedisTemplate.boundSetOps(key).add(value);
    }

    public Long srem(String key, String value) {
        return stringRedisTemplate.boundSetOps(key).remove(value);
    }

    public Long scard(String key) {
        return stringRedisTemplate.boundSetOps(key).size();
    }

    public Boolean sismember(String key, String value) {
        return stringRedisTemplate.boundSetOps(key).isMember(value);
    }

    public String brpop(int timeout, String key) {
        return stringRedisTemplate.boundListOps(key).rightPop(timeout, TimeUnit.SECONDS);
    }

    public Long lpush(String key, String value) {
        return stringRedisTemplate.boundListOps(key).leftPush(value);
    }

    public List<String> lrange(String key, int start, int end) {
        return stringRedisTemplate.boundListOps(key).range(start, end);
    }

    public Boolean zadd(String key, double score, String value) {
        return stringRedisTemplate.boundZSetOps(key).add(value, score);
    }

    public Long zrem(String key, String value) {
        return stringRedisTemplate.boundZSetOps(key).remove(value);
    }

    public Set<String> zrange(String key, int start, int end) {
        return stringRedisTemplate.boundZSetOps(key).range(start, end);
    }

    public Set<String> zrevrange(String key, int start, int end) {
        return stringRedisTemplate.boundZSetOps(key).range(start, end);
    }

    public Long zcard(String key) {
        return stringRedisTemplate.boundZSetOps(key).size();
    }

    public Double zscore(String key, String member) {
        return stringRedisTemplate.boundZSetOps(key).score(member);
    }

    public void flushDB(){
        stringRedisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.flushDb();
            return "ok";
        });
    }
}
