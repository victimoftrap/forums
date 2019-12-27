package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.dao.SessionDao;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.UserSession;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component("sessionDao")
public class SessionDaoImpl extends MapperCreatorDao implements SessionDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionDaoImpl.class);
    private final SqlSessionFactory sqlSessionFactory;

    @Autowired
    public SessionDaoImpl(final SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public void upsertSession(UserSession session) throws ServerException {
        LOGGER.debug("Creating new session for user {}", session.getUser());

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getSessionMapper(sqlSession).upsertSession(session);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to save session token in database {} for user {}",
                        session.getToken(), session.getUser(), ex
                );

                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }

    @Override
    public UserSession getSessionByToken(String token) throws ServerException {
        LOGGER.debug("Getting user session by session token {}", token);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getSessionMapper(sqlSession).getSessionByToken(token);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get user session by token {}", token, ex);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public User getUserByToken(String token) throws ServerException {
        LOGGER.debug("Getting user by session token {}", token);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getSessionMapper(sqlSession).getUserByToken(token);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get user by session token {}", token, ex);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public void deleteSession(String token) throws ServerException {
        LOGGER.debug("Deleting user session by token {}", token);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getSessionMapper(sqlSession).deleteByToken(token);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to delete user session by token {}", token, ex);

                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }

    @Override
    public void deleteAll() throws ServerException {
        LOGGER.debug("Deleting all sessions for all users");

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getSessionMapper(sqlSession).deleteAll();
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to delete all sessions for users", ex);

                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }
}
