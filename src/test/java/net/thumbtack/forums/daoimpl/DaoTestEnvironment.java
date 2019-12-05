package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.dao.*;
import net.thumbtack.forums.utils.MyBatisConnectionUtils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class DaoTestEnvironment {
    protected final UserDao userDao = new UserDaoImpl();
    protected final SessionDao sessionDao = new SessionDaoImpl();
    protected final ForumDao forumDao = new ForumDaoImpl();
    protected final MessageTreeDao messageTreeDao = new MessageTreeDaoImpl();
    protected final MessageDao messageDao = new MessageDaoImpl();
    protected final MessageHistoryDao messageHistoryDao = new MessageHistoryDaoImpl();

    @BeforeAll
    static void setupDatabase() {
        MyBatisConnectionUtils.initSqlSessionFactory();
    }

    @BeforeEach
    void clearDatabase() {
        userDao.deleteAll();
        messageDao.deleteAll();
    }
}
