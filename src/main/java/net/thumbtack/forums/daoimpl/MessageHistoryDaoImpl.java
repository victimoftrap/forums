package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.model.HistoryItem;
import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.dao.MessageHistoryDao;
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
    public HistoryItem saveNewVersion(MessageItem item) {
        LOGGER.debug("Saving new version of message with ID {}", item.getId());
        final HistoryItem newVersion = item.getHistory().get(0);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getMessageHistoryMapper(sqlSession).saveHistory(item.getId(), newVersion);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to save new version of message {}", item.getId(), ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
        return newVersion;
    }

    @Override
    public void editLatestVersion(MessageItem item) {
        LOGGER.debug("Updating unpublished version of message with ID {}", item.getId());

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getMessageHistoryMapper(sqlSession).editUnpublishedHistory(
                        item.getId(), item.getHistory().get(0)
                );
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to update unpublished version of message {}", item.getId());
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
                LOGGER.info("Unable to delete unpublished version by message ID {}", messageId, ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }
}
