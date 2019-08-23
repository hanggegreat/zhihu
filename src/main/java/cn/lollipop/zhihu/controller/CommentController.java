package cn.lollipop.zhihu.controller;

import cn.lollipop.zhihu.async.EventModel;
import cn.lollipop.zhihu.async.EventProducer;
import cn.lollipop.zhihu.async.EventType;
import cn.lollipop.zhihu.model.Comment;
import cn.lollipop.zhihu.model.EntityType;
import cn.lollipop.zhihu.model.HostHolder;
import cn.lollipop.zhihu.service.CommentService;
import cn.lollipop.zhihu.service.QuestionService;
import cn.lollipop.zhihu.util.WendaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

@Slf4j
@Controller
public class CommentController {
    private final HostHolder hostHolder;
    private final CommentService commentService;
    private final QuestionService questionService;
    private final EventProducer eventProducer;


    @Autowired
    public CommentController(HostHolder hostHolder, CommentService commentService, QuestionService questionService, EventProducer eventProducer) {
        this.hostHolder = hostHolder;
        this.commentService = commentService;
        this.questionService = questionService;
        this.eventProducer = eventProducer;
    }

    @RequestMapping(path = {"/addComment"}, method = {RequestMethod.POST})
    public String addComment(@RequestParam("questionId") int questionId,
                             @RequestParam("content") String content) {
        try {
            Comment comment = new Comment();
            comment.setContent(content);
            if (hostHolder.getUser() != null) {
                comment.setUserId(hostHolder.getUser().getId());
            } else {
                comment.setUserId(WendaUtil.ANONYMOUS_USERID);
                return "redirect:/reglogin";
            }
            comment.setCreatedDate(new Date());
            comment.setEntityType(EntityType.ENTITY_QUESTION);
            comment.setEntityId(questionId);
            commentService.addComment(comment);

            int count = commentService.getCommentCount(comment.getEntityId(), comment.getEntityType());
            questionService.updateCommentCount(comment.getEntityId(), count);

            eventProducer.fireEvent(new EventModel(EventType.COMMENT).setActorId(comment.getUserId())
                    .setEntityId(questionId));

        } catch (Exception e) {
            log.error("增加评论失败" + e.getMessage());
        }
        return "redirect:/question/" + questionId;
    }
}
