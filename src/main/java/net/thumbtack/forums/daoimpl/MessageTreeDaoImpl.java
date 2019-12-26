package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.dao.MessageTreeDao;
import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.model.MessageTree;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;

import net.thumbtack.forums.model.enums.MessageOrder;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Component("messageTreeDao")
public class MessageTreeDaoImpl extends MapperCreatorDao implements MessageTreeDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageTreeDaoImpl.class);
    private final SqlSessionFactory sqlSessionFactory;

    @Autowired
    public MessageTreeDaoImpl(final SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public MessageTree saveMessageTree(MessageTree tree) throws ServerException {
        LOGGER.debug("Creating new message tree with subject {} in forum {}",
                tree.getSubject(), tree.getForum().getId()
        );

        try(SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                final MessageItem rootMessage = tree.getRootMessage();
                getMessageTreeMapper(sqlSession).saveMessageTree(tree);
                getMessageMapper(sqlSession).saveMessageItem(rootMessage);
                getMessageHistoryMapper(sqlSession).saveHistory(
                        rootMessage.getId(), rootMessage.getHistory().get(0)
                );
                if (!tree.getTags().isEmpty()) {
                    getTagMapper(sqlSession).saveMessageForAllTags(tree);
                }
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to create new tree in forum {}", tree.getForum().getId(), ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
        return tree;
    }

    @Override
    public MessageTree newBranch(MessageTree tree) throws ServerException {
        LOGGER.debug("Creating new tree from message with ID {} in forum {}",
                tree.getRootMessage().getId(), tree.getForum().getId()
        );

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getMessageTreeMapper(sqlSession).saveMessageTree(tree);
                if (!tree.getTags().isEmpty()) {
                    getTagMapper(sqlSession).saveMessageForAllTags(tree);
                }
                getMessageMapper(sqlSession).madeTreeRootMessage(tree.getRootMessage());
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to made new branch from message {}", tree.getRootMessage().getId(), ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
        return tree;
    }

    @Override
    public MessageTree getMessageTreeById(int id) throws ServerException {
        LOGGER.debug("Getting root message with ID {}", id);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getMessageTreeMapper(sqlSession).getTreeById(id);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get root message with ID {}", id, ex);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public List<MessageTree> getTreesByForum(int forumId, MessageOrder order, int offset, int limit)
            throws ServerException {
        LOGGER.debug("Getting {} trees started from {} of forum with ID {} ordered {}",
                limit, offset, forumId, order
        );

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                return getMessageTreeMapper(sqlSession).getTreeList(forumId, order, offset, limit);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get trees of forum {}", forumId, ex);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public void changeBranchPriority(MessageTree tree) throws ServerException {
        LOGGER.debug("Changing priority of message tree with ID {} in tree {}",
                tree.getRootMessage().getId(), tree.getId()
        );

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getMessageTreeMapper(sqlSession).updateMessagePriority(tree);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to change priority of message tree with ID {}", tree.getId(), ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }

    @Override
    public void deleteTreeById(int id) throws ServerException {
        LOGGER.debug("Deleting message tree by ID {}", id);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getMessageTreeMapper(sqlSession).deleteTreeById(id);
                // histories and message item would be deleted by ON DELETE CASCADE
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to delete message tree by ID {}", id, ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }

    @Override
    public void deleteTreeByRootMessageId(int messageId) throws ServerException {
        LOGGER.debug("Deleting message tree with root message ID {}", messageId);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getMessageTreeMapper(sqlSession).deleteTreeByRootMessageId(messageId);
                // histories and message item would be deleted by ON DELETE CASCADE
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to delete message tree with root message ID {}", messageId, ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }
}
