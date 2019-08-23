package cn.lollipop.zhihu;

import cn.lollipop.zhihu.dao.QuestionDAO;
import cn.lollipop.zhihu.dao.UserDAO;
import cn.lollipop.zhihu.model.EntityType;
import cn.lollipop.zhihu.model.Question;
import cn.lollipop.zhihu.model.User;
import cn.lollipop.zhihu.service.FollowService;
import cn.lollipop.zhihu.service.SensitiveService;
import cn.lollipop.zhihu.util.RedisAdapter;
import cn.lollipop.zhihu.util.WendaUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.Random;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@Sql("/init-schema.sql")
public class InitDatabaseTests {
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private QuestionDAO questionDAO;

    @Autowired
    private SensitiveService sensitiveUtil;

    @Autowired
    private FollowService followService;

    @Autowired
    private RedisAdapter redisAdapter;

    @Test
    public void contextLoads() {
        Random random = new Random();
        redisAdapter.flushDB();
        for (int i = 0; i < 11; ++i) {
            User user = new User();
            user.setHeadUrl(String.format("http://images.nowcoder.com/head/%dt.png", random.nextInt(1000)));
            user.setName(String.format("USER%d", i+1));
            user.setPassword("");
            user.setSalt("");
            userDAO.addUser(user);

            for (int j = 1; j < i; ++j) {
                followService.follow(j, EntityType.ENTITY_USER, i);
            }

            user.setPassword(WendaUtil.MD5("159637428"));
            userDAO.updatePassword(user);

            Question question = new Question();
            question.setCommentCount(i);
            Date date = new Date();
            date.setTime(date.getTime() + 1000 * 3600 * 5 * i);
            question.setCreatedDate(date);
            question.setUserId(i + 1);
            question.setTitle(String.format("TITLE{%d}", i));
            question.setContent(String.format("Balaababalalalal Content %d", i));
            questionDAO.addQuestion(question);
        }

    }

    /*
    @Test
    public void testSensitive() {
        String content = "question content <img src=\"https:\\/\\/baidu.com/ff.png\">色情赌博";
        String result = sensitiveUtil.filter(content);
        System.out.println(result);
    }*/
}
