package net.thumbtack.forums.dao;

import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.UserSession;

public interface SessionDao {
    void upsertSession(UserSession session) throws ServerException;

    UserSession getSessionByToken(String token) throws ServerException;

    User getUserByToken(String token) throws ServerException;

    void deleteSession(String token) throws ServerException;

    void deleteAll() throws ServerException;
}
