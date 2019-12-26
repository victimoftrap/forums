package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.model.HistoryItem;
import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.dao.MessageHistoryDao;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Component("messageHistoryDao")
public class MessageHistoryDaoImpl extends MapperCreatorDao implements MessageHistoryDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageHistoryDaoImpl.class);
    private final SqlSessionFactory sqlSessionFactory;

    @Autowired
    public MessageHistoryDaoImpl(final SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public HistoryItem saveNewVersion(MessageItem item) throws ServerException {
        LOGGER.debug("Saving new version of message with ID {}", item.getId());
        final HistoryItem newVersion = item.getHistory().get(0);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
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
    public List<HistoryItem> getMessageHistory(int messageId,
                                               boolean allVersions,
                                               boolean unpublished) throws ServerException {
        LOGGER.debug("Getting {} {} history of message with ID {}",
                allVersions ? "all" : "latest", unpublished ? "with unpublished" : "published", messageId
        );

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getMessageHistoryMapper(sqlSession)
                        .getMessageHistoryByOptions(messageId, allVersions, unpublished);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get history of message {}", messageId, ex);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public void editLatestVersion(MessageItem item) throws ServerException {
        LOGGER.debug("Updating unpublished version of message with ID {}", item.getId());

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
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
    public void unpublishNewVersionBy(int messageId) throws ServerException {
        LOGGER.debug("Deleting unpublished version of message with ID {}", messageId);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
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
