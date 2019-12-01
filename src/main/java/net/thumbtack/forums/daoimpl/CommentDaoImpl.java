package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.dao.CommentDao;
import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.utils.MyBatisConnectionUtils;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("commentDao")
public class CommentDaoImpl extends MapperCreatorDao implements CommentDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentDaoImpl.class);

    @Override
    public MessageItem saveComment(MessageItem item) {
        LOGGER.debug("Saving new comment {}", item);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getMessageMapper(sqlSession).saveComment(item);
                getMessageHistoryMapper(sqlSession).saveAllHistory(item);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to save new comment {}", item, ex);
                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
        return item;
    }
}
