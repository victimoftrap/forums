package net.thumbtack.forums.dao;

import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.UserSession;

public interface SessionDao {
    void createSession(UserSession session);

    UserSession getSessionByToken(String token);

    String getSessionToken(User user);

    void deleteSession(String token);
}
