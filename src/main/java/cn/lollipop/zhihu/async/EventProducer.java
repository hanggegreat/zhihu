package cn.lollipop.zhihu.async;

import cn.lollipop.zhihu.util.RedisAdapter;
import cn.lollipop.zhihu.util.RedisKeyUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventProducer {
    private final RedisAdapter redisAdapter;

    @Autowired
    public EventProducer(RedisAdapter redisAdapter) {
        this.redisAdapter = redisAdapter;
    }

    public boolean fireEvent(EventModel eventModel) {
        try {
            String json = JSONObject.toJSONString(eventModel);
            String key = RedisKeyUtil.getEventQueueKey();
            redisAdapter.lpush(key, json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
