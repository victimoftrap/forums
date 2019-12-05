package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.UserSession;
import net.thumbtack.forums.model.enums.UserRole;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SessionDaoImplTest extends DaoTestEnvironment {
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
        final UserSession session = new UserSession(user, token);
        sessionDao.upsertSession(session);

        final UserSession foundedSession = sessionDao.getSessionByToken(token);
        assertEquals(session, foundedSession);
    }

    @Test
    void testCreateSession_sessionForUserAlreadyExists_sessionTokenChanged() {
        final User user = new User(
                UserRole.USER,
                "shermental", "shermental@gmail.com", "passwd",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user);

        final String firstToken = UUID.randomUUID().toString();
        final UserSession firstSession = new UserSession(user, firstToken);
        sessionDao.upsertSession(firstSession);

        final String secondToken = UUID.randomUUID().toString();
        final UserSession secondSession = new UserSession(user, secondToken);
        sessionDao.upsertSession(secondSession);

        assertNull(sessionDao.getSessionByToken(firstToken));
        assertNull(sessionDao.getUserByToken(firstToken));

        final User foundUser = sessionDao.getUserByToken(secondToken);
        assertEquals(user, foundUser);
        final UserSession foundSession = sessionDao.getSessionByToken(secondToken);
        assertEquals(secondSession, foundSession);
    }

    @Test
    void testGetUserBySessionToken() {
        final User user = new User(
                UserRole.USER,
                "shermental", "shermental@gmail.com", "passwd",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user);

        final String token = UUID.randomUUID().toString();
        final UserSession session = new UserSession(user, token);
        sessionDao.upsertSession(session);

        final User foundUser = sessionDao.getUserByToken(token);
        assertEquals(user, foundUser);
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
        final UserSession session = new UserSession(user, token);
        sessionDao.upsertSession(session);

        sessionDao.deleteSession(token);
        assertNull(sessionDao.getSessionByToken(token));
    }

    @Test
    void testDeleteAllSessions() {
        final User user1 = new User(
                UserRole.USER,
                "shermental", "shermental@gmail.com", "passwd",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user1);
        final String token1 = UUID.randomUUID().toString();
        final UserSession session1 = new UserSession(user1, token1);
        sessionDao.upsertSession(session1);

        final User user2 = new User(
                UserRole.USER,
                "jolywonka", "jolywonka@gmail.com", "jolywonka",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user2);
        final String token2 = UUID.randomUUID().toString();
        final UserSession session2 = new UserSession(user2, token2);
        sessionDao.upsertSession(session2);

        sessionDao.deleteAll();
        assertNull(sessionDao.getSessionByToken(token1));
        assertNull(sessionDao.getUserByToken(token1));
        assertNull(sessionDao.getSessionByToken(token2));
        assertNull(sessionDao.getUserByToken(token2));
    }
}