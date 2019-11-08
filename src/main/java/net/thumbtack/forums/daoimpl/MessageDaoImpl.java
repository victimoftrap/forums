package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.model.Message;
import net.thumbtack.forums.dao.MessageDao;
import net.thumbtack.forums.utils.MyBatisConnectionUtils;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageDaoImpl extends MapperCreatorDao implements MessageDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageDaoImpl.class);

    @Override
    public Message save(Message message) {
        LOGGER.debug("Saving new message {}", message);

        try (SqlSession session = MyBatisConnectionUtils.getSession()) {
            try {
                getMessageMapper(session).save(message);
            } catch (RuntimeException e) {
                LOGGER.error("Unable to save message {} in database {}", message, e);
                session.rollback();
                throw e;
            }
            session.commit();
        }
        return message;
    }

    @Override
    public Message findById(Integer id) {
        LOGGER.debug("Getting message by ID {}", id);

        try (SqlSession session = MyBatisConnectionUtils.getSession()) {
            try {
                return getMessageMapper(session).findById(id);
            } catch (RuntimeException e) {
                LOGGER.error("Unable to find message by ID {} {}", id, e);
                throw e;
            }
        }
    }

    @Override
    public void update(Message message) {
        LOGGER.debug("Updating message to {}", message);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getMessageMapper(sqlSession).update(message);
            } catch (RuntimeException e) {
                LOGGER.error("Unable to update message with ID {} {}", message.getId(), e);
                sqlSession.rollback();
                throw e;
            }
            sqlSession.commit();
        }
    }

    @Override
    public void deleteById(Integer id) {
        LOGGER.debug("Deleting message by ID {}", id);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getMessageMapper(sqlSession).deleteById(id);
            } catch (RuntimeException e) {
                LOGGER.error("Unable to delete message by ID {} {}", id, e);
                sqlSession.rollback();
                throw e;
            }
            sqlSession.commit();
        }
    }

    @Override
    public void deleteAll() {
        LOGGER.debug("Deleting all messages");

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getMessageMapper(sqlSession).deleteAll();
            } catch (RuntimeException e) {
                LOGGER.error("Unable to delete all messages from database", e);
                sqlSession.rollback();
                throw e;
            }
            sqlSession.commit();
        }
    }
}
