package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.dao.ForumDao;
import net.thumbtack.forums.model.Forum;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Component("forumDao")
public class ForumDaoImpl extends MapperCreatorDao implements ForumDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForumDaoImpl.class);
    private final SqlSessionFactory sqlSessionFactory;

    @Autowired
    public ForumDaoImpl(final SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public Forum save(Forum forum) throws ServerException {
        LOGGER.debug("Saving new forum in database {}", forum);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getForumMapper(sqlSession).save(forum);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to save forum {} in database", forum, ex);

                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
        return forum;
    }

    @Override
    public Forum getById(int id) throws ServerException {
        LOGGER.debug("Getting forum by ID {} from database", id);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getForumMapper(sqlSession).getById(id);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get forum by ID {}", id, ex);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public List<Forum> getAll() throws ServerException {
        LOGGER.debug("Getting all forums");

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getForumMapper(sqlSession).getAll();
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get all forums from database", ex);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public void update(Forum forum) throws ServerException {
        LOGGER.debug("Updating forum in database {}", forum);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getForumMapper(sqlSession).update(forum);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to update forum {} in database", forum, ex);

                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }

    @Override
    public void deleteById(int id) throws ServerException {
        LOGGER.debug("Deleting forum by ID {} in database", id);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getForumMapper(sqlSession).deleteById(id);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to delete forum by ID {}", id);

                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }

    @Override
    public void deleteAll() throws ServerException {
        LOGGER.debug("Deleting all forums from database");

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getForumMapper(sqlSession).deleteAll();
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to delete all forums");

                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }
}
