package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.model.*;
import net.thumbtack.forums.model.enums.ForumType;
import net.thumbtack.forums.model.enums.MessagePriority;
import net.thumbtack.forums.model.enums.MessageState;
import net.thumbtack.forums.model.enums.UserRole;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DebugDaoImplTest extends DaoTestEnvironment {
    private static User creator;
    private static Forum forum;
    private static MessageTree messageTree;
    private static MessageItem messageItem;
    private static HistoryItem version1;

    @BeforeAll
    static void createModels() {
        creator = new User(
                UserRole.USER,
                "user", "user@gmail.com", "passwd",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        forum = new Forum(
                ForumType.UNMODERATED, creator, "TestForum",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        version1 = new HistoryItem(
                "1st body", MessageState.PUBLISHED, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        messageTree = new MessageTree(
                forum, "TestTree", null, MessagePriority.NORMAL
        );
        messageItem = new MessageItem(
                creator, messageTree, null,
                Collections.singletonList(version1),
                version1.getCreatedAt(), version1.getCreatedAt()
        );
        messageTree.setRootMessage(messageItem);
    }

    @Test
    void testClearDatabase() {
        final UserSession creatorSession = new UserSession(creator, UUID.randomUUID().toString());
        userDao.save(creator, creatorSession);
        forumDao.save(forum);
        messageTreeDao.saveMessageTree(messageTree);

        final User commentCreator = new User(
                "otherUser", "other@email.com", "passwd"
        );
        final HistoryItem commentHistory = new HistoryItem(
                "comment body", MessageState.PUBLISHED, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem comment = new MessageItem(
                commentCreator, messageTree, messageItem, Collections.singletonList(commentHistory),
                commentHistory.getCreatedAt(), commentHistory.getCreatedAt()
        );
        final UserSession commentCreatorSession = new UserSession(commentCreator, UUID.randomUUID().toString());
        userDao.save(commentCreator, commentCreatorSession);
        messageDao.saveMessageItem(comment);

        debugDao.clear();
        assertNull(userDao.getById(creator.getId()));
        assertNull(userDao.getById(creator.getId(), true));
        assertNull(userDao.getById(commentCreator.getId()));
        assertNull(sessionDao.getUserByToken(creatorSession.getToken()));
        assertNull(sessionDao.getUserByToken(commentCreatorSession.getToken()));
        assertNull(forumDao.getById(forum.getId()));
        assertNull(messageDao.getMessageById(messageItem.getId()));
        assertNull(messageDao.getMessageById(comment.getId()));
    }
}