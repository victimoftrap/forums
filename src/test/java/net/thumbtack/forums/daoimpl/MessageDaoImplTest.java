package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.exception.ServerException;
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

import static org.junit.jupiter.api.Assertions.*;

class MessageDaoImplTest extends DaoTestEnvironment {
    private static User creator;
    private static Forum forum;
    private static MessageTree messageTree;
    private static MessageItem messageItem;
    private static HistoryItem singleHistory;

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
        singleHistory = new HistoryItem(
                "1st body", MessageState.UNPUBLISHED, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        messageTree = new MessageTree(
                forum, "TestTree", null,
                MessagePriority.NORMAL,
                singleHistory.getCreatedAt()
        );
        messageItem = new MessageItem(
                creator, messageTree, null,
                Collections.singletonList(singleHistory),
                singleHistory.getCreatedAt()
        );
        messageTree.setRootMessage(messageItem);
    }

    @Test
    void testCreateAndGetComment() throws ServerException {
        userDao.save(creator);
        forumDao.save(forum);
        messageTreeDao.saveMessageTree(messageTree);

        final User commentCreator = new User(
                "otherUser", "other@email.com", "passwd"
        );
        final HistoryItem commentHistory = new HistoryItem(
                "comment body", MessageState.PUBLISHED, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem comment = new MessageItem(
                commentCreator, messageTree, messageItem,
                Collections.singletonList(commentHistory),
                commentHistory.getCreatedAt()
        );
        userDao.save(commentCreator);
        messageDao.saveMessageItem(comment);

        final MessageItem rootMessage = messageDao.getMessageById(messageItem.getId());
        final List<MessageItem> commentsList = rootMessage.getChildrenComments();
        assertAll(
                () -> assertNotEquals(0, comment.getId()),
                () -> assertEquals(messageItem.getId(), rootMessage.getId()),
                () -> assertEquals(messageItem.getOwner(), rootMessage.getOwner()),
                () -> assertEquals(1, commentsList.size()),
                () -> assertEquals(comment.getId(), commentsList.get(0).getId()),
                () -> assertEquals(comment.getOwner(), commentsList.get(0).getOwner()),
                () -> assertEquals(comment.getHistory(), commentsList.get(0).getHistory()),
                () -> assertEquals(comment.getAverageRating(), commentsList.get(0).getAverageRating()),
                () -> assertEquals(comment.getCreatedAt(), commentsList.get(0).getCreatedAt()),
                () -> assertEquals(comment.getUpdatedAt(), commentsList.get(0).getUpdatedAt()),
                () -> assertEquals(comment.getParentMessage().getId(), commentsList.get(0).getParentMessage().getId()),
                () -> assertEquals(comment.getMessageTree().getId(), commentsList.get(0).getMessageTree().getId()),
                () -> assertEquals(comment.getChildrenComments(), commentsList.get(0).getChildrenComments())
        );
    }

    @Test
    void testGetRootMessageInTree() throws ServerException {
        userDao.save(creator);
        forumDao.save(forum);
        messageTreeDao.saveMessageTree(messageTree);

        final MessageItem selectedItem = messageDao.getMessageById(messageItem.getId());
        assertAll(
                () -> assertEquals(messageItem.getId(), selectedItem.getId()),
                () -> assertEquals(messageItem.getOwner(), selectedItem.getOwner()),
                () -> assertEquals(messageItem.getHistory(), selectedItem.getHistory()),
                () -> assertEquals(messageItem.getCreatedAt(), selectedItem.getCreatedAt()),
                () -> assertEquals(messageItem.getUpdatedAt(), selectedItem.getUpdatedAt()),
                () -> assertEquals(messageItem.getParentMessage(), selectedItem.getParentMessage()),
                () -> assertEquals(messageItem.getChildrenComments(), selectedItem.getChildrenComments()),
                () -> assertEquals(messageTree.getId(), selectedItem.getMessageTree().getId()),
                () -> assertEquals(messageTree.getSubject(), selectedItem.getMessageTree().getSubject()),
                () -> assertEquals(messageTree.getPriority(), selectedItem.getMessageTree().getPriority()),
                () -> assertEquals(messageTree.getRootMessage().getId(),
                        selectedItem.getMessageTree().getRootMessage().getId()
                )
        );
    }

    @Test
    void testGetRootMessageInTreeWithComments() throws ServerException {
        final User commentMaker = new User(
                "commentMaker", "user@gmail.com", "passwd"
        );
        userDao.save(creator);
        userDao.save(commentMaker);
        forumDao.save(forum);
        messageTreeDao.saveMessageTree(messageTree);

        final HistoryItem historyItem1 = new HistoryItem(
                "comment body 1", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem messageItem1 = new MessageItem(
                creator, messageTree, messageItem,
                Collections.singletonList(historyItem1),
                historyItem1.getCreatedAt()
        );
        messageDao.saveMessageItem(messageItem1);

        final HistoryItem historyItem2 = new HistoryItem(
                "comment body 2", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem messageItem2 = new MessageItem(
                commentMaker, messageTree, messageItem,
                Collections.singletonList(historyItem2),
                historyItem2.getCreatedAt()
        );
        messageDao.saveMessageItem(messageItem2);

        final MessageItem selectedItem = messageDao.getMessageById(messageItem.getId());
        assertAll(
                () -> assertEquals(messageItem.getId(), selectedItem.getId()),
                () -> assertEquals(messageItem.getOwner(), selectedItem.getOwner()),
                () -> assertEquals(messageItem.getHistory(), selectedItem.getHistory()),
                () -> assertEquals(messageItem.getCreatedAt(), selectedItem.getCreatedAt()),
                () -> assertEquals(messageItem.getUpdatedAt(), selectedItem.getUpdatedAt()),
                () -> assertEquals(messageItem.getParentMessage(), selectedItem.getParentMessage()),
                () -> assertFalse(selectedItem.getChildrenComments().isEmpty())
        );
    }

    @Test
    void testGetMessageWithMultipleVersions() throws ServerException {
        userDao.save(creator);
        forumDao.save(forum);

        final HistoryItem version1 = new HistoryItem(
                "1st body", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        messageItem = new MessageItem(
                creator, messageTree, null,
                Collections.singletonList(version1),
                version1.getCreatedAt()
        );
        messageTree.setRootMessage(messageItem);
        messageTreeDao.saveMessageTree(messageTree);

        final HistoryItem version2 = new HistoryItem(
                "version 2.0", MessageState.UNPUBLISHED,
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS)
                        .plus(1, ChronoUnit.HOURS)
        );
        List<HistoryItem> updatedVersions = Arrays.asList(version2, version1);
        messageItem.setHistory(updatedVersions);
        messageHistoryDao.saveNewVersion(messageItem);

        final HistoryItem version3 = new HistoryItem(
                "version 3.0", MessageState.UNPUBLISHED,
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS)
                        .plus(1, ChronoUnit.DAYS)
        );
        updatedVersions = Arrays.asList(version3, version2, version1);
        messageItem.setHistory(updatedVersions);
        messageHistoryDao.saveNewVersion(messageItem);

        final MessageItem selectedMessage = messageDao.getMessageById(messageItem.getId());
        assertEquals(messageItem.getId(), selectedMessage.getId());
        assertEquals(messageItem.getHistory(), selectedMessage.getHistory());
        assertEquals(version3, selectedMessage.getHistory().get(0));
        assertEquals(version2, selectedMessage.getHistory().get(1));
        assertEquals(version1, selectedMessage.getHistory().get(2));
    }

    @Test
    void testPublishMessage() throws ServerException {
        userDao.save(creator);
        forumDao.save(forum);
        messageTreeDao.saveMessageTree(messageTree);

        final MessageItem messageBeforePublication = messageDao.getMessageById(messageItem.getId());
        final HistoryItem unpublishedHistory = messageBeforePublication.getHistory().get(0);
        assertEquals(MessageState.UNPUBLISHED, singleHistory.getState());
        assertEquals(MessageState.UNPUBLISHED, unpublishedHistory.getState());

        singleHistory.setState(MessageState.PUBLISHED);
        messageDao.publish(messageItem);

        final MessageItem messageAfterPublication = messageDao.getMessageById(messageItem.getId());
        final HistoryItem publishedHistory = messageAfterPublication.getHistory().get(0);
        assertEquals(MessageState.PUBLISHED, publishedHistory.getState());
        assertNotEquals(unpublishedHistory.getState(), publishedHistory.getState());
    }

    @Test
    void testDeleteCommentById() throws ServerException {
        userDao.save(creator);
        forumDao.save(forum);
        messageTreeDao.saveMessageTree(messageTree);

        final User commentCreator = new User(
                "otherUser", "other@email.com", "passwd"
        );
        final HistoryItem commentHistory = new HistoryItem(
                "comment body", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem comment = new MessageItem(
                commentCreator, messageTree, messageItem,
                Collections.singletonList(commentHistory),
                commentHistory.getCreatedAt()
        );
        userDao.save(commentCreator);
        messageDao.saveMessageItem(comment);

        messageDao.deleteMessageById(comment.getId());
        final MessageItem deletedMessage = messageDao.getMessageById(comment.getId());
        assertNull(deletedMessage);
    }
}