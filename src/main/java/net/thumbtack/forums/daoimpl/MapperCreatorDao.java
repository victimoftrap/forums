package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.mappers.UserMapper;

import org.apache.ibatis.session.SqlSession;

public class MapperCreatorDao {
    protected UserMapper getUserMapper(final SqlSession sqlSession) {
        return sqlSession.getMapper(UserMapper.class);
    }
}
