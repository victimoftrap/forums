package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.UserSession;
import net.thumbtack.forums.model.enums.UserRole;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SessionDaoImplTest extends DaoTestBase {
    @Test
    void testSaveAndGetSession() {
        final User user = new User(
                UserRole.USER,
                "shermental", "shermental@gmail.com", "passwd",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user);

        final String token = UUID.randomUUID().toString();
        final UserSession session = new UserSession(
                user, token
        );
        sessionDao.createSession(session);

        final UserSession foundedSession = sessionDao.getSessionByToken(token);
        assertEquals(session, foundedSession);
    }

    @Test
    void testGetSessionToken() {
        final User user = new User(
                UserRole.USER,
                "shermental", "shermental@gmail.com", "passwd",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user);

        final String token = UUID.randomUUID().toString();
        final UserSession session = new UserSession(user, token);
        sessionDao.createSession(session);

        final String foundSessionToken = sessionDao.getSessionToken(user);
        assertEquals(token, foundSessionToken);
    }

    @Test
    void testDeleteSession() {
        final User user = new User(
                UserRole.USER,
                "shermental", "shermental@gmail.com", "passwd",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user);

        final String token = UUID.randomUUID().toString();
        final UserSession session = new UserSession(
                user, token
        );
        sessionDao.createSession(session);

        sessionDao.deleteSession(token);
        assertNull(sessionDao.getSessionByToken(token));
        assertNull(sessionDao.getSessionToken(user));
    }
}