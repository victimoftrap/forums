package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.model.User;
import net.thumbtack.forums.dao.UserDao;
import net.thumbtack.forums.utils.MyBatisConnectionUtils;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDaoImpl extends MapperCreatorDao implements UserDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public User save(User user) {
        LOGGER.debug("Saving new user {}", user);

        try (SqlSession session = MyBatisConnectionUtils.getSession()) {
            try {
                getUserMapper(session).save(user);
            } catch (RuntimeException e) {
                LOGGER.error("Unable to save user {}, {}", user, e);
                session.rollback();
                throw e;
            }
            session.commit();
        }
        return user;
    }

    @Override
    public User findById(Integer id) {
        LOGGER.debug("Getting user by ID {}", id);

        try (SqlSession session = MyBatisConnectionUtils.getSession()) {
            try {
                return getUserMapper(session).findById(id);
            } catch (RuntimeException e) {
                LOGGER.error("Unable to find user by ID () {}", id, e);
                throw e;
            }
        }
    }

    @Override
    public void update(User user) {
        LOGGER.debug("Updating user to {}", user);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getUserMapper(sqlSession).update(user);
            } catch (RuntimeException e) {
                LOGGER.error("Unable to update user with ID {} {}", user.getId(), e);
                sqlSession.rollback();
                throw e;
            }
            sqlSession.commit();
        }
    }

    @Override
    public void deleteById(Integer id) {
        LOGGER.debug("Deleting user by ID {}", id);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getUserMapper(sqlSession).deleteById(id);
            } catch (RuntimeException e) {
                LOGGER.error("Unable to delete user by ID {} {}", id, e);
                sqlSession.rollback();
                throw e;
            }
            sqlSession.commit();
        }
    }

    @Override
    public void deleteAll() {
        LOGGER.debug("Deleting all users");

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getUserMapper(sqlSession).deleteAll();
            } catch (RuntimeException e) {
                LOGGER.error("Unable to delete all users", e);
                sqlSession.rollback();
                throw e;
            }
            sqlSession.commit();
        }
    }
}
