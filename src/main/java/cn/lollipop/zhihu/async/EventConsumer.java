package cn.lollipop.zhihu.async;

import cn.lollipop.zhihu.util.RedisAdapter;
import cn.lollipop.zhihu.util.RedisKeyUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EventConsumer implements InitializingBean, ApplicationContextAware {
    private Map<EventType, List<EventHandler>> config = new HashMap<>();
    private ApplicationContext applicationContext;

    private final RedisAdapter redisAdapter;

    @Autowired
    public EventConsumer(RedisAdapter redisAdapter) {
        this.redisAdapter = redisAdapter;
    }

    @Override
    public void afterPropertiesSet() {
        Map<String, EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);
        if (beans != null) {
            for (Map.Entry<String, EventHandler> entry : beans.entrySet()) {
                List<EventType> eventTypes = entry.getValue().getSupportEventTypes();

                for (EventType type : eventTypes) {
                    if (!config.containsKey(type)) {
                        config.put(type, new ArrayList<>());
                    }
                    config.get(type).add(entry.getValue());
                }
            }
        }

        new Thread(() -> {
            while (true) {
                String key = RedisKeyUtil.getEventQueueKey();
                String message = redisAdapter.brpop(0, key);

                EventModel eventModel = JSON.parseObject(message, EventModel.class);
                if (!config.containsKey(eventModel.getType())) {
                    log.error("不能识别的事件");
                    continue;
                }

                for (EventHandler handler : config.get(eventModel.getType())) {
                    handler.doHandle(eventModel);
                }
            }
        }).start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
