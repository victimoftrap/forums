package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.mappers.UserMapper;
import net.thumbtack.forums.mappers.SessionMapper;
import net.thumbtack.forums.mappers.ForumMapper;
import net.thumbtack.forums.mappers.RatingMapper;

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
}
