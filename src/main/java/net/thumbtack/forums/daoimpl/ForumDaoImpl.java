package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.dao.ForumDao;
import net.thumbtack.forums.utils.MyBatisConnectionUtils;
import net.thumbtack.forums.model.Forum;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ForumDaoImpl extends MapperCreatorDao implements ForumDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForumDaoImpl.class);

    @Override
    public Forum save(Forum forum) {
        LOGGER.debug("Saving new forum in database {}", forum);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getForumMapper(sqlSession).save(forum);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to save forum {} in database", forum, ex);

                sqlSession.rollback();
                throw ex;
            }
            sqlSession.commit();
        }
        return forum;
    }

    @Override
    public Forum getById(int id) {
        LOGGER.debug("Getting forum by ID {} from database", id);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                return getForumMapper(sqlSession).getById(id);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get forum by ID {}", id, ex);
                throw ex;
            }
        }
    }

    @Override
    public List<Forum> getAll() {
        LOGGER.debug("Getting all forums");

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                return getForumMapper(sqlSession).getAll();
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get all forums from database", ex);
                throw ex;
            }
        }
    }

    @Override
    public void update(Forum forum) {
        LOGGER.debug("Updating forum in database {}", forum);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getForumMapper(sqlSession).update(forum);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to update forum {} in database", forum, ex);

                sqlSession.rollback();
                throw ex;
            }
            sqlSession.commit();
        }
    }

    @Override
    public void deleteById(int id) {
        LOGGER.debug("Deleting forum by ID {} in database", id);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getForumMapper(sqlSession).deleteById(id);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to delete forum by ID {}", id);

                sqlSession.rollback();
                throw ex;
            }
            sqlSession.commit();
        }
    }
}
