package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.model.Forum;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.enums.ForumType;
import net.thumbtack.forums.model.enums.UserRole;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class ForumDaoImplTest extends DaoTestBase {
    @Test
    void testSaveNewForum() {
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
    void testGetForumById() {
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
    void testGetForumById_forumNotExists_notFound() {
        Forum forum = forumDao.getById(481516);
        assertNull(forum);
    }

    @Test
    void testUpdateReadonlyForumFlag() {
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
    void testDeleteForum() {
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