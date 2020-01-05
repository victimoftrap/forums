package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.dao.MessageTreeDao;
import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.model.MessageTree;
import net.thumbtack.forums.model.enums.MessageOrder;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
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
        LOGGER.debug("Creating new message tree {}", tree);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                final MessageItem rootMessage = tree.getRootMessage();

                getMessageTreeMapper(sqlSession).saveMessageTree(tree);
                getMessageMapper(sqlSession).saveMessageItem(rootMessage);
                getMessageHistoryMapper(sqlSession).saveHistory(
                        rootMessage.getId(), rootMessage.getHistory().get(0)
                );

                if (!tree.getTags().isEmpty()) {
                    getTagMapper(sqlSession).saveAllTags(tree.getTags());
                    getTagMapper(sqlSession).safeBindMessageAndTags(tree);
                }
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to create new tree {}", tree, ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
        return tree;
    }

    @Override
    public MessageTree newBranch(MessageTree tree) throws ServerException {
        LOGGER.debug("Creating new tree from message {}", tree);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                getMessageTreeMapper(sqlSession).saveMessageTree(tree);
                if (!tree.getTags().isEmpty()) {
                    getTagMapper(sqlSession).saveAllTags(tree.getTags());
                    getTagMapper(sqlSession).safeBindMessageAndTags(tree);
                }
                getMessageMapper(sqlSession).madeTreeRootMessage(tree.getRootMessage());
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to made new branch from message {}", tree, ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
        return tree;
    }

    @Override
    public MessageItem getTreeRootMessage(
            int messageId, MessageOrder order, boolean noComments, boolean allVersions, boolean unpublished
    ) throws ServerException {
        LOGGER.debug(
                "Getting root message by ID {} with params: order={}, " +
                        "noComments={}, allVersions={}, unpublished={}",
                messageId, order.name(), noComments, allVersions, unpublished
        );

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                MessageItem rootMessage = getParametrizedMessageMapper(sqlSession)
                        .getRootMessage(messageId, order.name(), allVersions, unpublished);
                if (noComments) {
                    rootMessage.setChildrenComments(Collections.emptyList());
                }
                return rootMessage;
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get root message by ID {}", messageId, ex);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public List<MessageTree> getForumTrees(
            int forumId,
            boolean noComments, boolean allVersions, boolean unpublished,
            MessageOrder order, int offset, int limit
    ) throws ServerException {
        LOGGER.debug(
                "Getting messages with params: offset={}, limit={}, order={}, " +
                        "noComments={}, allVersions={}, unpublished={}",
                offset, limit, order.name(), noComments, allVersions, unpublished
        );

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                List<MessageTree> trees = getParametrizedMessageTreeMapper(sqlSession)
                        .getTrees(forumId, offset, limit, order.name(), allVersions, unpublished);
                if (noComments) {
                    trees.forEach(tree -> tree.getRootMessage()
                            .setChildrenComments(Collections.emptyList())
                    );
                }
                return trees;
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to get messages", ex);
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    @Override
    public void changeBranchPriority(MessageTree tree) throws ServerException {
        LOGGER.debug("Changing priority of message tree {}", tree);

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
