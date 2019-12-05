package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.dao.MessageHistoryDao;
import net.thumbtack.forums.model.HistoryItem;
import net.thumbtack.forums.utils.MyBatisConnectionUtils;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("messageHistoryDao")
public class MessageHistoryDaoImpl extends MapperCreatorDao implements MessageHistoryDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageHistoryDaoImpl.class);

    @Override
    public HistoryItem saveNewVersion(int messageId, HistoryItem history) {
        LOGGER.debug("Saving new version {} of message", history);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getMessageHistoryMapper(sqlSession).saveHistory(messageId, history);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to save new version {} of message", history, ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
        return history;
    }

    @Override
    public void editLatestVersion(int messageId, HistoryItem history) {
        LOGGER.debug("Updating unpublished version of message {}", history);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                LOGGER.info("Unable to update unpublished version {} of message", history);
                getMessageHistoryMapper(sqlSession).editUnpublishedHistory(messageId, history);
            } catch (RuntimeException ex) {
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }

    @Override
    public void unpublishNewVersionBy(int messageId) {
        LOGGER.debug("Deleting unpublished version of message with ID {}", messageId);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getMessageHistoryMapper(sqlSession).deleteRejectedHistory(messageId);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to delete message version by ID {}", messageId, ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }
}
