package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.dao.StatisticDao;
import net.thumbtack.forums.view.MessageRatingView;
import net.thumbtack.forums.view.MessagesCountView;
import net.thumbtack.forums.view.UserRatingView;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("statisticDao")
public class StatisticDaoImpl extends MapperCreatorDao implements StatisticDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticDaoImpl.class);
    private final SqlSessionFactory sqlSessionFactory;

    @Autowired
    public StatisticDaoImpl(final SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public int getMessagesCount() throws ServerException {
        LOGGER.debug("Getting messages count in server");

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getStatisticMapper(sqlSession).getMessagesCount();
            } catch (RuntimeException re) {
                LOGGER.info("Unable to get messages count in server", re);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public int getMessagesCount(int forumId) throws ServerException {
        LOGGER.debug("Getting messages count in forum with ID {}", forumId);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getForumMapper(sqlSession).getPublishedMessagesCount(forumId);
            } catch (RuntimeException re) {
                LOGGER.info("Unable to get messages count in forum with ID {}", forumId, re);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public int getCommentsCount() throws ServerException {
        LOGGER.debug("Getting comments count in server");

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getStatisticMapper(sqlSession).getCommentsCount();
            } catch (RuntimeException re) {
                LOGGER.info("Unable to get comments count in server", re);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public int getCommentsCount(int forumId) throws ServerException {
        LOGGER.debug("Getting comments count in forum with ID {}", forumId);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getForumMapper(sqlSession).getPublishedCommentCount(forumId);
            } catch (RuntimeException re) {
                LOGGER.info("Unable to get comments count in forum with ID {}", forumId, re);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public MessagesCountView getMessagesAndCommentsCount() throws ServerException {
        LOGGER.debug("Getting messages and comments count in server");

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getStatisticMapper(sqlSession).getMessagesAndCommentsCount();
            } catch (RuntimeException re) {
                LOGGER.info("Unable to get messages and comments count in server", re);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public MessagesCountView getMessagesAndCommentsCount(int forumId) throws ServerException {
        LOGGER.debug("Getting messages and comments count in forum with ID {}", forumId);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getStatisticMapper(sqlSession).getMessagesAndCommentsCountInForum(forumId);
            } catch (RuntimeException re) {
                LOGGER.info("Unable to get messages and comments count in forum with ID {}",
                        forumId, re
                );
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public List<MessageRatingView> getMessagesRatings(int offset, int limit) throws ServerException {
        LOGGER.debug("Getting messages ratings in server, offset={}, limit={}", offset, limit);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getStatisticMapper(sqlSession).getMessagesRatings(offset, limit);
            } catch (RuntimeException re) {
                LOGGER.info("Unable to get messages ratings", re);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public List<MessageRatingView> getMessagesRatingsInForum(
            int forumId, int offset, int limit
    ) throws ServerException {
        LOGGER.debug("Getting messages ratings in forum with ID {}, offset={}, limit={}",
                forumId, offset, limit
        );

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getStatisticMapper(sqlSession)
                        .getMessagesRatingsInForum(forumId, offset, limit);
            } catch (RuntimeException re) {
                LOGGER.info("Unable to get messages ratings in forum {}", forumId, re);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public List<UserRatingView> getUsersRatings(int offset, int limit) throws ServerException {
        LOGGER.debug("Getting users ratings in server, offset={}, limit={}", offset, limit);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getStatisticMapper(sqlSession).getUsersRatings(offset, limit);
            } catch (RuntimeException re) {
                LOGGER.info("Unable to get users ratings", re);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public List<UserRatingView> getUsersRatingsInForum(
            int forumId, int offset, int limit
    ) throws ServerException {
        LOGGER.debug("Getting users ratings in forum with ID {}, offset={}, limit={}",
                forumId, offset, limit
        );

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getStatisticMapper(sqlSession).getUsersRatingsInForum(forumId, offset, limit);
            } catch (RuntimeException re) {
                LOGGER.info("Unable to get users ratings in forum {}", forumId, re);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }
}
