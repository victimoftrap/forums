package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.model.MessageTree;
import net.thumbtack.forums.dao.MessageDao;
import net.thumbtack.forums.utils.MyBatisConnectionUtils;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("messageDao")
public class MessageDaoImpl extends MapperCreatorDao implements MessageDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageDaoImpl.class);

    @Override
    public MessageTree saveMessage(final MessageTree tree) {
        LOGGER.debug("Saving new message and his tree {}", tree);

        try(SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getMessageMapper(sqlSession).saveMessage(tree.getRootMessage());
                getMessageHistoryMapper(sqlSession).saveAllHistory(tree.getRootMessage());
                getMessageMapper(sqlSession).saveMessageTree(tree);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to save new message {}", tree, ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
        return tree;
    }
}
