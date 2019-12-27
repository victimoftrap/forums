package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.dao.DebugDao;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component("debugDao")
public class DebugDaoImpl extends MapperCreatorDao implements DebugDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(DebugDaoImpl.class);
    private final SqlSessionFactory sqlSessionFactory;

    @Autowired
    public DebugDaoImpl(final SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public void clear() throws ServerException {
        LOGGER.debug("Clearing database");

        try (SqlSession session = sqlSessionFactory.openSession()) {
            try {
                getUserMapper(session).deleteAll();
                getTagMapper(session).deleteAll();
                getMessageTreeMapper(session).deleteAll();
                getForumMapper(session).deleteAll();
            } catch (RuntimeException e) {
                LOGGER.info("Unable to clear database", e);
                session.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            session.commit();
        }
    }
}
