package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.dao.UserDao;
import net.thumbtack.forums.utils.MyBatisConnectionUtils;
import net.thumbtack.forums.model.User;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserDaoImpl extends MapperCreatorDao implements UserDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public User save(User user) {
        LOGGER.debug("Inserting new user in database {}", user);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getUserMapper(sqlSession).save(user);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to save user {} in database", user, ex);

                sqlSession.rollback();
                throw ex;
            }
            sqlSession.commit();
        }
        return user;
    }

    @Override
    public User getById(int id) {
        LOGGER.debug("Getting user by ID {} from database", id);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                return getUserMapper(sqlSession).getById(id);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get user by ID {}", id, ex);
                throw ex;
            }
        }
    }

    @Override
    public User getById(int id, boolean deleted) {
        LOGGER.debug("Getting user that can be deactivated by ID {}", id);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                return getUserMapper(sqlSession).getByIdAndDeleted(id, deleted);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get user by ID {} from database", id, ex);
                throw ex;
            }
        }
    }

    @Override
    public User getByName(String name) {
        LOGGER.debug("Getting user by name {} from database", name);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                return getUserMapper(sqlSession).getByName(name);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get user by name {}", name, ex);
                throw ex;
            }
        }
    }

    @Override
    public User getByName(String name, boolean deleted) {
        LOGGER.debug("Getting user that can be deactivated by name {}", name);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                return getUserMapper(sqlSession).getByNameAndDeleted(name, deleted);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get user by name {} from database", name, ex);
                throw ex;
            }
        }
    }

    @Override
    public List<User> getAll() {
        LOGGER.debug("Getting all users from database");

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                return getUserMapper(sqlSession).getAll();
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get users from database", ex);
                throw ex;
            }
        }
    }

    @Override
    public List<User> getAll(boolean withDeleted) {
        LOGGER.debug(String.format("Getting all %s users from database",
                withDeleted ? "existing and deleted" : "existing"
        ));

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                return getUserMapper(sqlSession).getAllAndDeleted(withDeleted);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get users from database", ex);
                throw ex;
            }
        }
    }

    @Override
    public void update(User user) {
        LOGGER.debug("Updating user in database {}", user);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getUserMapper(sqlSession).update(user);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to update user {} in database", user, ex);

                sqlSession.rollback();
                throw ex;
            }
            sqlSession.commit();
        }
    }

    @Override
    public void deactivateById(int id) {
        LOGGER.debug("Deactivating account of user bu ID {}", id);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getUserMapper(sqlSession).deactivateById(id);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to deactivate user by ID {}", id, ex);

                sqlSession.rollback();
                throw ex;
            }
            sqlSession.commit();
        }
    }

    @Override
    public void deleteAll() {
        LOGGER.debug("Deleting all users from database");

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getUserMapper(sqlSession).deleteAll();
            } catch (RuntimeException ex) {
                LOGGER.info("Unable delete all users from database", ex);

                sqlSession.rollback();
                throw ex;
            }
            sqlSession.commit();
        }
    }
}
