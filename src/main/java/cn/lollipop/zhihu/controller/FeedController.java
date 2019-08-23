package cn.lollipop.zhihu.controller;

import cn.lollipop.zhihu.model.EntityType;
import cn.lollipop.zhihu.model.Feed;
import cn.lollipop.zhihu.model.HostHolder;
import cn.lollipop.zhihu.service.FeedService;
import cn.lollipop.zhihu.service.FollowService;
import cn.lollipop.zhihu.util.RedisAdapter;
import cn.lollipop.zhihu.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

@Controller
public class FeedController {
    private final FeedService feedService;
    private final FollowService followService;
    private final HostHolder hostHolder;
    private final RedisAdapter redisAdapter;

    @Autowired
    public FeedController(FeedService feedService, FollowService followService, HostHolder hostHolder, RedisAdapter redisAdapter) {
        this.feedService = feedService;
        this.followService = followService;
        this.hostHolder = hostHolder;
        this.redisAdapter = redisAdapter;
    }

    @RequestMapping(path = {"/pushfeeds"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String getPushFeeds(Model model) {
        int localUserId = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
        List<String> feedIds = redisAdapter.lrange(RedisKeyUtil.getTimelineKey(localUserId), 0, 10);
        List<Feed> feeds = new ArrayList<>();
        for (String feedId : feedIds) {
            Feed feed = feedService.getById(Integer.parseInt(feedId));
            if (feed != null) {
                feeds.add(feed);
            }
        }
        model.addAttribute("feeds", feeds);
        return "feeds";
    }

    @RequestMapping(path = {"/pullfeeds"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String getPullFeeds(Model model) {
        int localUserId = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
        List<Integer> followees = new ArrayList<>();
        if (localUserId != 0) {
            // 关注的人
            followees = followService.getFollowees(localUserId, EntityType.ENTITY_USER, Integer.MAX_VALUE);
        }
        List<Feed> feeds = feedService.getUserFeeds(Integer.MAX_VALUE, followees, 10);
        model.addAttribute("feeds", feeds);
        return "feeds";
    }
}
