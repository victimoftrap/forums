package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.dao.MessageDao;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component("messageDao")
public class MessageDaoImpl extends MapperCreatorDao implements MessageDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageDaoImpl.class);
    private final SqlSessionFactory sqlSessionFactory;

    @Autowired
    public MessageDaoImpl(final SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public MessageItem saveMessageItem(MessageItem item) {
        LOGGER.debug("Saving new message in tree with ID {}", item.getMessageTree().getId());

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getMessageMapper(sqlSession).saveMessageItem(item);
                getMessageHistoryMapper(sqlSession).saveHistory(
                        item.getId(), item.getHistory().get(0)
                );
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to save new message in tree {}", item.getMessageTree().getId(), ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
        return item;
    }

    @Override
    public MessageItem getMessageById(int id) {
        LOGGER.debug("Getting message by ID {}", id);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getMessageMapper(sqlSession).getMessageById(id);
            } catch (RuntimeException ex) {
                LOGGER.debug("Unable to get message by ID {}", id, ex);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public void publish(MessageItem item) {
        LOGGER.debug("Publishing version of message with ID {}", item.getId());

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getMessageHistoryMapper(sqlSession).updateMessageHistory(item.getId(), item.getHistory().get(0));
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to publish message {}", item, ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }

    @Override
    public void deleteMessageById(int id) {
        LOGGER.debug("Deleting message by ID {}", id);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getMessageMapper(sqlSession).deleteById(id);
                // histories would be deleted by ON DELETE CASCADE
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to delete message by ID {}", id, ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }

    @Override
    public void deleteAll() {
        LOGGER.debug("Deleting all messages from database");

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getMessageMapper(sqlSession).deleteAll();
                // histories and message tree would be deleted by ON DELETE CASCADE
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to delete all messages", ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }
}
