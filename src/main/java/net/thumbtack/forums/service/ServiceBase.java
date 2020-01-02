package net.thumbtack.forums.service;

import net.thumbtack.forums.configuration.ServerConfigurationProperties;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.Forum;
import net.thumbtack.forums.dao.ForumDao;
import net.thumbtack.forums.dao.SessionDao;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;

public abstract class ServiceBase {
    private SessionDao sessionDao;
    private ForumDao forumDao;
    private ServerConfigurationProperties serverProperties;

    public ServiceBase(final SessionDao sessionDao,
                       final ForumDao forumDao,
                       final ServerConfigurationProperties serverProperties) {
        this.sessionDao = sessionDao;
        this.forumDao = forumDao;
        this.serverProperties = serverProperties;
    }

    protected User getUserBySession(final String token) throws ServerException {
        final User user = sessionDao.getUserByToken(token);
        if (user == null) {
            throw new ServerException(ErrorCode.WRONG_SESSION_TOKEN);
        }
        return user;
    }

    protected Forum getForumById(final int id) throws ServerException {
        final Forum forum = forumDao.getById(id);
        if (forum == null) {
            throw new ServerException(ErrorCode.FORUM_NOT_FOUND);
        }
        return forum;
    }

    protected void checkUserBanned(final User user) throws ServerException {
        if (user.getBannedUntil() != null) {
            throw new ServerException(ErrorCode.USER_BANNED);
        }
    }

    protected void checkUserBannedPermanently(final User user) throws ServerException {
        if (user.getBanCount() == serverProperties.getMaxBanCount()) {
            throw new ServerException(ErrorCode.USER_PERMANENTLY_BANNED);
        }
    }
}
