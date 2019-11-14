package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.dao.DebugDao;
import net.thumbtack.forums.utils.MyBatisConnectionUtils;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugDaoImpl extends MapperCreatorDao implements DebugDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(DebugDaoImpl.class);

    @Override
    public void clear() {
        LOGGER.debug("Clearing database");

        try (SqlSession session = MyBatisConnectionUtils.getSession()) {
            try {
                getUserMapper(session).deleteAll();
            } catch (RuntimeException e) {
                LOGGER.info("Unable to clear database", e);
                session.rollback();
                throw e;
            }
            session.commit();
        }
    }
}
