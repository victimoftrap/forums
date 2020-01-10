package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.dao.UserDao;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.UserSession;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.exceptions.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;

@Component("userDao")
public class UserDaoImpl extends MapperCreatorDao implements UserDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDaoImpl.class);
    private final SqlSessionFactory sqlSessionFactory;

    @Autowired
    public UserDaoImpl(final SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public User save(User user) throws ServerException {
        LOGGER.debug("Inserting new user in database {}", user);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getUserMapper(sqlSession).save(user);
            } catch (PersistenceException e) {
                if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                    LOGGER.info("Username already used {} {}", user, e.getMessage());
                    sqlSession.rollback();
                    throw new ServerException(ErrorCode.USER_NAME_ALREADY_USED);
                }
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to save user {}", user, ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
        return user;
    }

    @Override
    public UserSession save(User user, UserSession session) throws ServerException {
        LOGGER.debug("Saving new user and creating session for him {}", session);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getUserMapper(sqlSession).save(user);
                getSessionMapper(sqlSession).upsertSession(session);
            } catch (PersistenceException e) {
                if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                    LOGGER.info("Username already used {} {}", session, e.getMessage());
                    sqlSession.rollback();
                    throw new ServerException(ErrorCode.USER_NAME_ALREADY_USED);
                }
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to save user and session {}", session, ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
        return session;
    }

    @Override
    public User getById(int id) throws ServerException {
        LOGGER.debug("Getting user by ID {} from database", id);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getUserMapper(sqlSession).getById(id);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get user by ID {}", id, ex);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    // REVU зачем 2 метода. Можно скрипт использовать <if.. AND deleted 
    public User getById(int id, boolean deleted) throws ServerException {
        LOGGER.debug("Getting user that can be deactivated by ID {}", id);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getUserMapper(sqlSession).getByIdAndDeleted(id, deleted);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get user by ID {} from database", id, ex);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public User getByName(String name) throws ServerException {
        LOGGER.debug("Getting user by name {} from database", name);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getUserMapper(sqlSession).getByName(name);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get user by name {}", name, ex);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public User getByName(String name, boolean deleted) throws ServerException {
        LOGGER.debug("Getting user that can be deactivated by name {}", name);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getUserMapper(sqlSession).getByNameAndDeleted(name, deleted);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get user by name {} from database", name, ex);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public List<User> getAll() throws ServerException {
        LOGGER.debug("Getting all users from database");

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getUserMapper(sqlSession).getAll();
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get users from database", ex);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    // REVU см. выше
    public List<User> getAll(boolean withDeleted) throws ServerException {
        LOGGER.debug(String.format("Getting all %s users from database",
                withDeleted ? "existing and deleted" : "existing"
        ));

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getUserMapper(sqlSession).getAllAndDeleted(withDeleted);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get users from database", ex);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public List<UserSession> getAllWithSessions() throws ServerException {
        LOGGER.debug("Getting all users with they sessions from database");

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getUserMapper(sqlSession).getAllWithSessions();
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get users and sessions from database", ex);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public void update(User user) throws ServerException {
        LOGGER.debug("Updating user in database {}", user);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getUserMapper(sqlSession).update(user);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to update user {} in database", user, ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }

    @Override
    public void madeSuperuser(User user) throws ServerException {
        LOGGER.debug("Grant role superuser for {}", user);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getUserMapper(sqlSession).update(user);
                getUserMapper(sqlSession).unbanUser(user);
                getForumMapper(sqlSession).changeReadonlyFlagModeratedForums(user.getId(), false);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to unban user {}", user, ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }

    @Override
    public void banUser(User user, boolean isPermanent) throws ServerException {
        LOGGER.debug("Banning user {}, {}", isPermanent ? "permanent" : "", user);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getUserMapper(sqlSession).banUser(user);
                if (isPermanent) {
                    getForumMapper(sqlSession).changeReadonlyFlagModeratedForums(user.getId(), true);
                }
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to ban user with ID {}", user.getId(), ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }

    @Override
    public void unbanAllByDate(LocalDateTime date) throws ServerException {
        LOGGER.debug("Unban all users that need to be unbanned on {}", date);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getUserMapper(sqlSession).unbanAllByDate(date);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to unban users on date {}", date, ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }

    @Override
    public void deactivateById(int id) throws ServerException {
        LOGGER.debug("Deactivating user account by ID {} and deleting his session", id);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getForumMapper(sqlSession).madeReadonlyModeratedForumsOfUser(id);
                getSessionMapper(sqlSession).deleteByUser(id);
                getUserMapper(sqlSession).deactivateById(id);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to deactivate user by ID {}", id, ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }

    @Override
    public void deleteAll() throws ServerException {
        LOGGER.debug("Deleting all users from database");

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getUserMapper(sqlSession).deleteAllWithoutAdmin();
            } catch (RuntimeException ex) {
                LOGGER.info("Unable delete all users from database", ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }
}
