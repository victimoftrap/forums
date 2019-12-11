package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.dao.MessageTreeDao;
import net.thumbtack.forums.model.MessageTree;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.utils.MyBatisConnectionUtils;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("messageTreeDao")
public class MessageTreeDaoImpl extends MapperCreatorDao implements MessageTreeDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageTreeDaoImpl.class);

    @Override
    public MessageTree saveMessageTree(MessageTree tree) {
        LOGGER.debug("Creating new message tree with subject {} in forum {}",
                tree.getSubject(), tree.getForum().getId()
        );

        try(SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getMessageTreeMapper(sqlSession).saveMessageTree(tree);
                getMessageMapper(sqlSession).saveMessageItem(tree.getRootMessage());
                getMessageHistoryMapper(sqlSession).saveAllHistory(tree.getRootMessage());
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
    public MessageTree newBranch(MessageTree tree) {
        LOGGER.debug("Creating new tree from message with ID {} in forum {}",
                tree.getRootMessage().getId(), tree.getForum().getId()
        );

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
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
    public void changeBranchPriority(MessageTree tree) {
        LOGGER.debug("Changing priority of message tree with ID {} in tree {}",
                tree.getRootMessage().getId(), tree.getId()
        );

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
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
    public void deleteTreeById(int id) {
        LOGGER.debug("Deleting message tree by ID {}", id);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
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
    public void deleteTreeByRootMessageId(int messageId) {
        LOGGER.debug("Deleting message tree with root message ID {}", messageId);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
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
