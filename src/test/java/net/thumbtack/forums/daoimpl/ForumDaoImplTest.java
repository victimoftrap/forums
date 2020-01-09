package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.model.*;
import net.thumbtack.forums.model.enums.ForumType;
import net.thumbtack.forums.model.enums.MessagePriority;
import net.thumbtack.forums.model.enums.MessageState;
import net.thumbtack.forums.model.enums.UserRole;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user1);

        Forum forum1 = new Forum(
                ForumType.UNMODERATED,
                user1,
                "USED_NAME",
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS),
                false,
                0, 0
        );
        forumDao.save(forum1);

        User user2 = new User(
                UserRole.USER,
                "e.foreman", "house_lite@gmail.com", "thirteen",
                LocalDateTime.now()
                        .plus(1, ChronoUnit.WEEKS)
                        .truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user2);

        Forum forumWithUsedName = new Forum(
                ForumType.UNMODERATED,
                user2,
                "USED_NAME",
                LocalDateTime.now()
                        .plus(1, ChronoUnit.WEEKS)
                        .truncatedTo(ChronoUnit.SECONDS),
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
    void testGetForumById_forumHasMessagesAndComments() throws ServerException {
        User forumOwner = new User(
                UserRole.USER,
                "g.house", "greg.house@gmail.com", "cuddy",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(forumOwner);

        Forum forum = new Forum(
                ForumType.UNMODERATED,
                forumOwner,
                "TestForum",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false,
                0, 0
        );
        forumDao.save(forum);

        HistoryItem message1History = new HistoryItem(
                "Message #1", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        MessageTree message1Tree = new MessageTree(
                forum, "Tree #1", null,
                MessagePriority.NORMAL,
                message1History.getCreatedAt()
        );
        MessageItem message1 = new MessageItem(
                forumOwner, message1Tree, null,
                Collections.singletonList(message1History),
                message1History.getCreatedAt()
        );
        message1Tree.setRootMessage(message1);
        messageTreeDao.saveMessageTree(message1Tree);

        HistoryItem message2History1 = new HistoryItem(
                "Message #2", MessageState.PUBLISHED,
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS)
        );
        MessageTree message2Tree = new MessageTree(
                forum, "Tree #2", null,
                MessagePriority.NORMAL,
                message2History1.getCreatedAt()
        );
        MessageItem message2 = new MessageItem(
                forumOwner, message2Tree, null,
                Collections.singletonList(message2History1),
                message2History1.getCreatedAt()
        );
        message2Tree.setRootMessage(message2);
        messageTreeDao.saveMessageTree(message2Tree);

        HistoryItem message2History2 = new HistoryItem(
                "Message #2 VERSION 2.0", MessageState.PUBLISHED,
                LocalDateTime.now()
                        .plus(1, ChronoUnit.MINUTES)
                        .truncatedTo(ChronoUnit.SECONDS)
        );
        final List<HistoryItem> updatedVersions = Arrays.asList(message2History2, message2History1);
        message2.setHistory(updatedVersions);
        messageHistoryDao.saveNewVersion(message2);

        HistoryItem comment1History = new HistoryItem(
                "Comment #1 -> Message #1", MessageState.PUBLISHED,
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS)
        );
        MessageItem comment1 = new MessageItem(
                forumOwner, message1Tree, message1,
                Collections.singletonList(comment1History),
                comment1History.getCreatedAt()
        );
        messageDao.saveMessageItem(comment1);

        HistoryItem comment2History = new HistoryItem(
                "Comment #2 -> Message #1", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        MessageItem comment2 = new MessageItem(
                forumOwner, message1Tree, message1,
                Collections.singletonList(comment2History),
                comment2History.getCreatedAt()
        );
        messageDao.saveMessageItem(comment2);

        HistoryItem comment3History = new HistoryItem(
                "Comment #3 -> Comment #2", MessageState.PUBLISHED,
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS)
        );
        MessageItem comment3 = new MessageItem(
                forumOwner, message1Tree, comment2,
                Collections.singletonList(comment3History),
                comment3History.getCreatedAt()
        );
        messageDao.saveMessageItem(comment3);

        HistoryItem comment4History = new HistoryItem(
                "Comment #4 -> Message #2", MessageState.PUBLISHED,
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS)
        );
        MessageItem comment4 = new MessageItem(
                forumOwner, message2Tree, message2,
                Collections.singletonList(comment4History),
                comment4History.getCreatedAt()
        );
        messageDao.saveMessageItem(comment4);

        HistoryItem comment5History = new HistoryItem(
                "Comment #5 -> Comment #4", MessageState.UNPUBLISHED,
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS)
        );
        MessageItem comment5 = new MessageItem(
                forumOwner, message1Tree, comment4,
                Collections.singletonList(comment5History),
                comment5History.getCreatedAt()
        );
        messageDao.saveMessageItem(comment5);

        Forum selectedForum = forumDao.getById(forum.getId());
        assertEquals(forum.getId(), selectedForum.getId());
        assertEquals(forum.getType(), selectedForum.getType());
        assertEquals(forum.getName(), selectedForum.getName());
        assertEquals(forum.getOwner(), selectedForum.getOwner());
        assertEquals(forum.isReadonly(), selectedForum.isReadonly());
        assertEquals(forum.getCreatedAt(), selectedForum.getCreatedAt());
        assertEquals(2, selectedForum.getMessageCount());
        assertEquals(4, selectedForum.getCommentCount());
    }

    @Test
    void testGetForumById_forumNotExists_notFound() throws ServerException {
        Forum forum = forumDao.getById(481516);
        assertNull(forum);
    }

    @Test
    void testGetAllForums() throws ServerException {
        User user1 = new User("User1", "user1@mail.com", "abcde");
        userDao.save(user1);

        Forum forum1 = new Forum(
                ForumType.MODERATED, user1, "ForumName #1",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        forumDao.save(forum1);

        User user2 = new User("User2", "user2@mail.com", "pass-pass");
        userDao.save(user2);

        Forum forum2 = new Forum(
                ForumType.UNMODERATED, user2, "ForumName #2",
                LocalDateTime
                        .now()
                        .plus(1, ChronoUnit.WEEKS)
                        .truncatedTo(ChronoUnit.SECONDS)
        );
        forumDao.save(forum2);

        Forum forum3 = new Forum(
                ForumType.MODERATED, user1, "ForumName #3",
                LocalDateTime
                        .now()
                        .plus(3, ChronoUnit.MONTHS)
                        .truncatedTo(ChronoUnit.SECONDS)
        );
        forumDao.save(forum3);

        List<Forum> forumsInServer = forumDao.getAll();
        assertEquals(Arrays.asList(forum1, forum2, forum3), forumsInServer);
    }

    @Test
    void testGetAllForums_noForumsInServer_shouldReturnEmptyList() throws ServerException {
        List<Forum> forumList = forumDao.getAll();
        assertEquals(Collections.emptyList(), forumList);
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

    @Test
    void testDeleteAllForums() throws ServerException {
        User user1 = new User("User1", "user1@mail.com", "abcde");
        userDao.save(user1);

        Forum forum1 = new Forum(
                ForumType.MODERATED, user1, "ForumName #1",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        forumDao.save(forum1);

        User user2 = new User("User2", "user2@mail.com", "pass-pass");
        userDao.save(user2);

        Forum forum2 = new Forum(
                ForumType.UNMODERATED, user2, "ForumName #2",
                LocalDateTime
                        .now()
                        .plus(1, ChronoUnit.WEEKS)
                        .truncatedTo(ChronoUnit.SECONDS)
        );
        forumDao.save(forum2);

        Forum forum3 = new Forum(
                ForumType.MODERATED, user1, "ForumName #3",
                LocalDateTime
                        .now()
                        .plus(3, ChronoUnit.MONTHS)
                        .truncatedTo(ChronoUnit.SECONDS)
        );
        forumDao.save(forum3);

        List<Forum> forumsBeforeDeletion = forumDao.getAll();

        forumDao.deleteAll();
        List<Forum> forumsAfterDeletion = forumDao.getAll();
        assertEquals(3, forumsBeforeDeletion.size());
        assertEquals(Collections.emptyList(), forumsAfterDeletion);
    }
}