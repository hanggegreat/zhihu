package cn.lollipop.zhihu.async.handler;

import cn.lollipop.zhihu.async.EventHandler;
import cn.lollipop.zhihu.async.EventModel;
import cn.lollipop.zhihu.async.EventType;
import cn.lollipop.zhihu.model.EntityType;
import cn.lollipop.zhihu.model.Feed;
import cn.lollipop.zhihu.model.Question;
import cn.lollipop.zhihu.model.User;
import cn.lollipop.zhihu.service.FeedService;
import cn.lollipop.zhihu.service.FollowService;
import cn.lollipop.zhihu.service.QuestionService;
import cn.lollipop.zhihu.service.UserService;
import cn.lollipop.zhihu.util.RedisAdapter;
import cn.lollipop.zhihu.util.RedisKeyUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FeedHandler implements EventHandler {
    private final FollowService followService;
    private final UserService userService;
    private final FeedService feedService;
    private final RedisAdapter redisAdapter;
    private final QuestionService questionService;

    @Autowired
    public FeedHandler(FollowService followService, UserService userService, FeedService feedService, RedisAdapter redisAdapter, QuestionService questionService) {
        this.followService = followService;
        this.userService = userService;
        this.feedService = feedService;
        this.redisAdapter = redisAdapter;
        this.questionService = questionService;
    }


    private String buildFeedData(EventModel model) {
        Map<String, String> map = new HashMap<>();
        // 触发用户是通用的
        User actor = userService.getUser(model.getActorId());
        if (actor == null) {
            return null;
        }
        map.put("userId", String.valueOf(actor.getId()));
        map.put("userHead", actor.getHeadUrl());
        map.put("userName", actor.getName());

        if (model.getType() == EventType.COMMENT ||
                (model.getType() == EventType.FOLLOW  && model.getEntityType() == EntityType.ENTITY_QUESTION)) {
            Question question = questionService.getById(model.getEntityId());
            if (question == null) {
                return null;
            }
            map.put("questionId", String.valueOf(question.getId()));
            map.put("questionTitle", question.getTitle());
            return JSONObject.toJSONString(map);
        }
        return null;
    }

    @Override
    public void doHandle(EventModel model) {
        // 为了测试，把model的userId随机一下
        Random r = new Random();
        model.setActorId(1+r.nextInt(10));

        // 构造一个新鲜事
        Feed feed = new Feed();
        feed.setCreatedDate(new Date());
        feed.setType(model.getType().getValue());
        feed.setUserId(model.getActorId());
        feed.setData(buildFeedData(model));
        if (feed.getData() == null) {
            return;
        }
        feedService.addFeed(feed);

        // 获得所有粉丝
        List<Integer> followers = followService.getFollowers(EntityType.ENTITY_USER, model.getActorId(), Integer.MAX_VALUE);
        // 系统队列
        followers.add(0);
        // 给所有粉丝推事件
        for (int follower : followers) {
            String timelineKey = RedisKeyUtil.getTimelineKey(follower);
            redisAdapter.lpush(timelineKey, String.valueOf(feed.getId()));
            // 限制最长长度，如果timelineKey的长度过大，就删除后面的新鲜事
        }
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.COMMENT, EventType.FOLLOW);
    }
}
