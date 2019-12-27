package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.dao.RatingDao;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("ratingDao")
public class RatingDaoImpl extends MapperCreatorDao implements RatingDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(RatingDaoImpl.class);
    private final SqlSessionFactory sqlSessionFactory;

    @Autowired
    public RatingDaoImpl(final SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public void upsertRating(MessageItem message, User user, int rating) throws ServerException {
        LOGGER.debug("Inserting or updating rating for message {} from user {}",
                message.getId(), user.getId()
        );

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getRatingMapper(sqlSession).upsertRating(message, user, rating);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to upsert for message {}", message.getId(), ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }

    @Override
    public void rate(MessageItem message, User user, int rating) throws ServerException {
        LOGGER.debug("Saving new rate for message {} from user {}", message.getId(), user.getId());

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getRatingMapper(sqlSession).rate(message, user, rating);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to save new rate for message {}", message.getId(), ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }

    @Override
    public void changeRating(MessageItem message, User user, int rating) throws ServerException {
        LOGGER.debug("Changing rating for message {} from user {}", message.getId(), user.getId());

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getRatingMapper(sqlSession).changeRating(message, user, rating);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to change rating for message {}", message.getId(), ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }

    @Override
    public void deleteRate(MessageItem message, User user) throws ServerException {
        LOGGER.debug("Deleting rating for message {} from user {}", message.getId(), user.getId());

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getRatingMapper(sqlSession).deleteRate(message, user);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to delete rating for message {}", message.getId(), ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }

    @Override
    public double getMessageRating(MessageItem item) throws ServerException {
        LOGGER.debug("Getting rating of message with ID {}", item.getId());
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getRatingMapper(sqlSession).getMessageRating(item.getId());
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get rating of message {}", item.getId(), ex);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }
}
