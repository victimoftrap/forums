package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.mappers.*;

import org.apache.ibatis.session.SqlSession;

public class MapperCreatorDao {
    protected UserMapper getUserMapper(final SqlSession sqlSession) {
        return sqlSession.getMapper(UserMapper.class);
    }

    protected SessionMapper getSessionMapper(final SqlSession sqlSession) {
        return sqlSession.getMapper(SessionMapper.class);
    }

    protected ForumMapper getForumMapper(final SqlSession sqlSession) {
        return sqlSession.getMapper(ForumMapper.class);
    }

    protected RatingMapper getRatingMapper(final SqlSession sqlSession) {
        return sqlSession.getMapper(RatingMapper.class);
    }

    protected TagMapper getTagMapper(final SqlSession sqlSession) {
        return sqlSession.getMapper(TagMapper.class);
    }

    protected MessageTreeMapper getMessageTreeMapper(final SqlSession sqlSession) {
        return sqlSession.getMapper(MessageTreeMapper.class);
    }

    protected MessageMapper getMessageMapper(final SqlSession sqlSession) {
        return sqlSession.getMapper(MessageMapper.class);
    }

    protected MessageHistoryMapper getMessageHistoryMapper(final SqlSession sqlSession) {
        return sqlSession.getMapper(MessageHistoryMapper.class);
    }

    protected ParametrizedMessageMapper getParametrizedMessageMapper(final SqlSession sqlSession) {
        return sqlSession.getMapper(ParametrizedMessageMapper.class);
    }
    protected ParametrizedMessageTreeMapper getParametrizedMessageTreeMapper(final SqlSession sqlSession) {
        return sqlSession.getMapper(ParametrizedMessageTreeMapper.class);
    }
}
