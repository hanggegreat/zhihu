package cn.lollipop.zhihu.async.handler;

import cn.lollipop.zhihu.async.EventHandler;
import cn.lollipop.zhihu.async.EventModel;
import cn.lollipop.zhihu.async.EventType;
import cn.lollipop.zhihu.util.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.util.*;

@Component
public class LoginExceptionHandler implements EventHandler {
    private final MailService mailService;

    @Autowired
    public LoginExceptionHandler(MailService mailService) {
        this.mailService = mailService;
    }

    @Override
    public void doHandle(EventModel model) {
        // 判断发现这个用户登陆异常
        try {
            mailService.sendHtmlMail(model.getExt("email"), "登陆IP异常", "你好" + model.getExt("username") + "，你的登陆有问题!");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Collections.singletonList(EventType.LOGIN);
    }
}
