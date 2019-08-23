package cn.lollipop.zhihu.async.handler;

import cn.lollipop.zhihu.async.EventHandler;
import cn.lollipop.zhihu.async.EventModel;
import cn.lollipop.zhihu.async.EventType;
import cn.lollipop.zhihu.model.Message;
import cn.lollipop.zhihu.model.User;
import cn.lollipop.zhihu.service.MessageService;
import cn.lollipop.zhihu.service.UserService;
import cn.lollipop.zhihu.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class LikeHandler implements EventHandler {
    private final MessageService messageService;
    private final UserService userService;

    @Autowired
    public LikeHandler(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    @Override
    public void doHandle(EventModel model) {
        Message message = new Message();
        message.setFromId(WendaUtil.SYSTEM_USERID);
        message.setToId(model.getEntityOwnerId());
        message.setCreatedDate(new Date());
        User user = userService.getUser(model.getActorId());
        message.setContent("用户" + user.getName()
                + "赞了你的评论,http://127.0.0.1:8080/question/" + model.getExt("questionId"));

        messageService.addMessage(message);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Collections.singletonList(EventType.LIKE);
    }
}
