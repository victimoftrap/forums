package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.dao.MessageHistoryDao;
import net.thumbtack.forums.model.HistoryItem;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.utils.MyBatisConnectionUtils;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MessageHistoryDaoImpl extends MapperCreatorDao implements MessageHistoryDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageHistoryDaoImpl.class);

    @Override
    public HistoryItem save(HistoryItem history) {
        LOGGER.debug("Saving new version of message body {}", history);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getMessageHistoryMapper(sqlSession).save(history);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to save new version of message {}", history, ex);

                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
        return history;
    }

    @Override
    // getMessageHistory
    public List<HistoryItem> getHistoryOfMessage(int messageId, boolean allVersions, boolean unpublished) {
        String logMessage;
        if (allVersions) {
            logMessage = "Getting all versions of message";
        } else {
            logMessage = String.format("Getting current %s version of message",
                    unpublished ? "unpublished" : "published"
            );
        }
        LOGGER.debug(logMessage);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                return getMessageHistoryMapper(sqlSession).getHistories(messageId, allVersions, unpublished);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get message history", ex);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public void update(HistoryItem history) {
        LOGGER.debug("Updating history of message body to {}", history);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getMessageHistoryMapper(sqlSession).update(history);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to update history item of message {}", history, ex);

                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }

    @Override
    public void delete(HistoryItem history) {
        LOGGER.debug("Deleting version of message body {}", history);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getMessageHistoryMapper(sqlSession).delete(history);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to delete history item of message {}", history, ex);

                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }

    @Override
    public void deleteAll() {
        LOGGER.debug("Deleting all histories of all messages");

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getMessageHistoryMapper(sqlSession).deleteAll();
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to delete histories", ex);

                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }
}
