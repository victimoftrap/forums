package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.model.*;
import net.thumbtack.forums.model.enums.UserRole;
import net.thumbtack.forums.model.enums.ForumType;
import net.thumbtack.forums.model.enums.MessageState;
import net.thumbtack.forums.model.enums.MessagePriority;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageHistoryDaoImplTest extends DaoTestEnvironment {
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
                "1st body", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        messageTree = new MessageTree(
                forum, "TestTree", null, MessagePriority.NORMAL
        );
    }

    @Test
    void testSaveNewMessageVersion() throws ServerException {
        userDao.save(creator);
        forumDao.save(forum);

        messageItem = new MessageItem(
                creator, messageTree, null,
                Collections.singletonList(version1),
                version1.getCreatedAt()
        );
        messageTree.setRootMessage(messageItem);
        messageTreeDao.saveMessageTree(messageTree);

        final HistoryItem version2 = new HistoryItem(
                "version 2.0", MessageState.PUBLISHED,
                LocalDateTime
                        .now()
                        .plus(1, ChronoUnit.DAYS)
                        .truncatedTo(ChronoUnit.SECONDS)
        );
        final List<HistoryItem> updatedVersions = Arrays.asList(version2, version1);
        messageItem.setHistory(updatedVersions);
        messageHistoryDao.saveNewVersion(messageItem);

        final MessageItem selectedMessage = messageDao.getMessageById(messageItem.getId());
        assertEquals(messageItem.getId(), selectedMessage.getId());
        assertEquals(messageItem.getHistory(), selectedMessage.getHistory());
    }

    @Test
    void testEditLatestUnpublishedVersion() throws ServerException {
        userDao.save(creator);
        forumDao.save(forum);

        messageItem = new MessageItem(
                creator, messageTree, null,
                Collections.singletonList(version1),
                version1.getCreatedAt()
        );
        messageTree.setRootMessage(messageItem);
        messageTreeDao.saveMessageTree(messageTree);

        final HistoryItem version2 = new HistoryItem(
                "version 2.0", MessageState.UNPUBLISHED,
                LocalDateTime
                        .now()
                        .plus(1, ChronoUnit.DAYS)
                        .truncatedTo(ChronoUnit.SECONDS)
        );
        final List<HistoryItem> updatedVersions = Arrays.asList(version2, version1);
        messageItem.setHistory(updatedVersions);
        messageHistoryDao.saveNewVersion(messageItem);

        version2.setBody("version 3.0");
        messageHistoryDao.editLatestVersion(messageItem);

        final MessageItem selectedMessage = messageDao.getMessageById(messageItem.getId());
        final HistoryItem unpublishedAfter = messageItem.getHistory().get(0);
        assertEquals(messageItem.getId(), selectedMessage.getId());
        assertEquals(version2, unpublishedAfter);
        assertEquals(1 , selectedMessage.getMessageTree().getForum().getMessageCount());
    }

    @Test
    void testUnpublishLatestVersion() throws ServerException {
        userDao.save(creator);
        forumDao.save(forum);

        messageItem = new MessageItem(
                creator, messageTree, null,
                Collections.singletonList(version1),
                version1.getCreatedAt()
        );
        messageTree.setRootMessage(messageItem);
        messageTreeDao.saveMessageTree(messageTree);

        final HistoryItem version2 = new HistoryItem(
                "version 2.0", MessageState.UNPUBLISHED,
                LocalDateTime
                        .now()
                        .plus(1, ChronoUnit.DAYS)
                        .truncatedTo(ChronoUnit.SECONDS)
        );
        final List<HistoryItem> updatedVersions = Arrays.asList(version2, version1);
        messageItem.setHistory(updatedVersions);
        messageHistoryDao.saveNewVersion(messageItem);

        messageHistoryDao.unpublishNewVersionBy(messageItem.getId());

        final MessageItem selectedMessage = messageDao.getMessageById(messageItem.getId());
        assertEquals(messageItem.getId(), selectedMessage.getId());
        assertEquals(2, messageItem.getHistory().size());
        assertEquals(1, selectedMessage.getHistory().size());
        assertEquals(messageItem.getHistory().get(1), selectedMessage.getHistory().get(0));
    }
}