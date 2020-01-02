package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.model.Forum;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.enums.ForumType;
import net.thumbtack.forums.model.enums.UserRole;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class ForumDaoImplTest extends DaoTestEnvironment {
    @Test
    void testSaveNewForum() throws ServerException {
        User user = new User(
                UserRole.USER,
                "g.house", "greg.house@gmail.com", "cuddy",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user);

        Forum forum = new Forum(
                ForumType.UNMODERATED,
                user,
                "medicine",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false,
                0, 0
        );
        Forum savedForum = forumDao.save(forum);

        assertAll(
                () -> assertNotEquals(0, forum.getId()),
                () -> assertEquals(forum, savedForum)
        );
    }

    @Test
    void testSaveNewForum_forumNameAlreadyUser_shouldThrowException() throws ServerException {
        User user1 = new User(
                UserRole.USER,
                "g.house", "greg.house@gmail.com", "cuddy",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user1);

        Forum forum1 = new Forum(
                ForumType.UNMODERATED,
                user1,
                "USED_NAME",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false,
                0, 0
        );
        forumDao.save(forum1);

        User user2 = new User(
                UserRole.USER,
                "e.foreman", "house_lite@gmail.com", "thirteen",
                LocalDateTime.now().plus(1, ChronoUnit.WEEKS).truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user2);

        Forum forumWithUsedName = new Forum(
                ForumType.UNMODERATED,
                user2,
                "USED_NAME",
                LocalDateTime.now().plus(1, ChronoUnit.WEEKS).truncatedTo(ChronoUnit.SECONDS),
                false,
                0, 0
        );

        try {
            forumDao.save(forumWithUsedName);
        } catch (ServerException se) {
            assertEquals(ErrorCode.FORUM_NAME_ALREADY_USED, se.getErrorCode());
        }
    }

    @Test
    void testGetForumById() throws ServerException {
        User user = new User(
                UserRole.USER,
                "g.house", "greg.house@gmail.com", "cuddy",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user);

        Forum forum = new Forum(
                ForumType.UNMODERATED,
                user,
                "medicine",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false,
                0, 0
        );
        forumDao.save(forum);

        Forum selectedForum = forumDao.getById(forum.getId());
        assertEquals(forum, selectedForum);
    }

    @Test
    void testGetForumById_forumNotExists_notFound() throws ServerException {
        Forum forum = forumDao.getById(481516);
        assertNull(forum);
    }

    @Test
    void testUpdateReadonlyForumFlag() throws ServerException {
        User user = new User(
                UserRole.USER,
                "g.house", "greg.house@gmail.com", "cuddy",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user);

        Forum forum = new Forum(
                ForumType.UNMODERATED,
                user,
                "medicine",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false,
                0, 0
        );
        forumDao.save(forum);

        final Forum updatedForum = new Forum(
                forum.getId(),
                forum.getType(),
                null,
                null,
                null,
                true,
                0, 0
        ) ;
        forumDao.update(updatedForum);

        final Forum selectedForum = forumDao.getById(forum.getId());
        assertNotEquals(forum, selectedForum);
        assertTrue(selectedForum.isReadonly());
    }

    @Test
    void testDeleteForum() throws ServerException {
        User user = new User(
                UserRole.USER,
                "g.house", "greg.house@gmail.com", "cuddy",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user);

        Forum forum = new Forum(
                ForumType.UNMODERATED,
                user,
                "medicine",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false,
                0, 0
        );
        forumDao.save(forum);

        Forum selectedFirstly = forumDao.getById(forum.getId());
        assertEquals(forum, selectedFirstly);

        forumDao.deleteById(forum.getId());
        Forum selectedSecondly = forumDao.getById(forum.getId());
        assertNull(selectedSecondly);
    }
}