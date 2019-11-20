package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.dao.BanDao;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.utils.MyBatisConnectionUtils;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BanDaoImpl extends MapperCreatorDao implements BanDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(BanDaoImpl.class);

    @Override
    public void banUser(User user) {
        LOGGER.debug("Ban user {} in server", user);

        try (SqlSession sqlSession = MyBatisConnectionUtils.getSession()) {
            try {
                getUserMapper(sqlSession).update(user);
            } catch (RuntimeException ex) {
                LOGGER.info("Unable to ban user {}", user, ex);

                sqlSession.rollback();
                throw new ServerException(ErrorCode.DATABASE_ERROR);
            }
            sqlSession.commit();
        }
    }
}
