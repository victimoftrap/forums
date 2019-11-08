package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.dao.UserDao;
import net.thumbtack.forums.dao.MessageDao;
import net.thumbtack.forums.utils.MyBatisConnectionUtils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class DaoTestBase {
    protected final UserDao userDao = new UserDaoImpl();
    protected final MessageDao messageDao = new MessageDaoImpl();

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
