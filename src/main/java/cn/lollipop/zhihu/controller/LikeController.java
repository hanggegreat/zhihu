package cn.lollipop.zhihu.controller;

import cn.lollipop.zhihu.async.EventModel;
import cn.lollipop.zhihu.async.EventProducer;
import cn.lollipop.zhihu.async.EventType;
import cn.lollipop.zhihu.model.Comment;
import cn.lollipop.zhihu.model.EntityType;
import cn.lollipop.zhihu.model.HostHolder;
import cn.lollipop.zhihu.service.CommentService;
import cn.lollipop.zhihu.service.LikeService;
import cn.lollipop.zhihu.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LikeController {
    private final LikeService likeService;
    private final HostHolder hostHolder;
    private final CommentService commentService;
    private final EventProducer eventProducer;

    @Autowired
    public LikeController(LikeService likeService, HostHolder hostHolder, CommentService commentService, EventProducer eventProducer) {
        this.likeService = likeService;
        this.hostHolder = hostHolder;
        this.commentService = commentService;
        this.eventProducer = eventProducer;
    }

    @RequestMapping(path = {"/like"}, method = {RequestMethod.POST})
    @ResponseBody
    public String like(@RequestParam("commentId") int commentId) {
        if (hostHolder.getUser() == null) {
            return WendaUtil.getJSONString(999);
        }

        Comment comment = commentService.getCommentById(commentId);

        eventProducer.fireEvent(new EventModel(EventType.LIKE)
                .setActorId(hostHolder.getUser().getId()).setEntityId(commentId)
                .setEntityType(EntityType.ENTITY_COMMENT).setEntityOwnerId(comment.getUserId())
                .setExt("questionId", String.valueOf(comment.getEntityId())));

        long likeCount = likeService.like(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT, commentId);
        return WendaUtil.getJSONString(0, String.valueOf(likeCount));
    }

    @RequestMapping(path = {"/dislike"}, method = {RequestMethod.POST})
    @ResponseBody
    public String dislike(@RequestParam("commentId") int commentId) {
        if (hostHolder.getUser() == null) {
            return WendaUtil.getJSONString(999);
        }

        long likeCount = likeService.disLike(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT, commentId);
        return WendaUtil.getJSONString(0, String.valueOf(likeCount));
    }
}
