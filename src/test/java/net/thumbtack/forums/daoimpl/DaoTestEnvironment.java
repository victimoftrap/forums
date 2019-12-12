package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.dao.*;
import net.thumbtack.forums.utils.MyBatisConnectionUtils;

import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class DaoTestEnvironment {
    private static SqlSessionFactory sqlSessionFactory;

    protected final UserDao userDao = new UserDaoImpl(sqlSessionFactory);
    protected final SessionDao sessionDao = new SessionDaoImpl(sqlSessionFactory);
    protected final ForumDao forumDao = new ForumDaoImpl(sqlSessionFactory);
    protected final MessageTreeDao messageTreeDao = new MessageTreeDaoImpl(sqlSessionFactory);
    protected final MessageDao messageDao = new MessageDaoImpl(sqlSessionFactory);
    protected final MessageHistoryDao messageHistoryDao = new MessageHistoryDaoImpl(sqlSessionFactory);
    protected final RatingDao ratingDao = new RatingDaoImpl(sqlSessionFactory);

    @BeforeAll
    static void setupDatabase() {
        MyBatisConnectionUtils.createSqlSessionFactory();
        sqlSessionFactory = MyBatisConnectionUtils.getSqlSessionFactory();
    }

    @BeforeEach
    void clearDatabase() {
        userDao.deleteAll();
        messageDao.deleteAll();
    }
}
