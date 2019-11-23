package net.thumbtack.forums.dao;

import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.UserSession;

public interface SessionDao {
    void upsertSession(UserSession session);

    UserSession getSessionByToken(String token);

    User getUserByToken(String token);

    void deleteSession(String token);

    void deleteAll();
}
