package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.dao.SessionDao;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.UserSession;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.utils.MyBatisConnectionUtils;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionDaoImpl extends MapperCreatorDao implements SessionDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionDaoImpl.class);

    @Override
    public void createSession(UserSession session) {
        LOGGER.debug("Creating new session for user {}", session.getUser());

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getSessionMapper(sqlSession).save(session);
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
    public UserSession getSessionByToken(String token) {
        LOGGER.debug("Getting user session by token {}", token);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                return getSessionMapper(sqlSession).getByToken(token);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get user session by token {}", token, ex);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public String getSessionToken(User user) {
        LOGGER.debug("Getting session token for user {}", user);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                return getSessionMapper(sqlSession).getSessionToken(user);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get session token for user {}", user, ex);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public void deleteSession(String token) {
        LOGGER.debug("Deleting user session by token {}", token);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getSessionMapper(sqlSession).deleteByToken(token);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to delete user session by token {}", token, ex);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }
}
