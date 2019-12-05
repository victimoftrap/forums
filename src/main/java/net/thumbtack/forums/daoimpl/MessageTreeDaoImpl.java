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
    public MessageTree newMessageTree(MessageTree tree) {
        LOGGER.debug("Creating new message tree {} in forum", tree);

        try(SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getMessageMapper(sqlSession).saveMessageItem(tree.getRootMessage());
                getMessageHistoryMapper(sqlSession).saveAllHistory(tree.getRootMessage());
                getMessageTreeMapper(sqlSession).saveMessageTree(tree);
                if (!tree.getTags().isEmpty()) {
                    getTagMapper(sqlSession).saveMessageForAllTags(tree);
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
    public MessageTree newBranch(MessageTree tree) {
        LOGGER.debug("Creating new tree {} from message {}", tree, tree.getRootMessage());

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getMessageTreeMapper(sqlSession).saveMessageTree(tree);
                if (!tree.getTags().isEmpty()) {
                    getTagMapper(sqlSession).saveMessageForAllTags(tree);
                }
                getMessageMapper(sqlSession).deleteParentMessage(tree.getRootMessage().getId());
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to made new branch {} from message", tree, ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
        return tree;
    }

    @Override
    public void changeBranchPriority(MessageTree tree) {
        LOGGER.debug("Changing priority of root message in tree {}", tree);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getMessageTreeMapper(sqlSession).updateMessagePriority(tree);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to change priority of {}", tree, ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }
}
