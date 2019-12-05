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
import java.util.Collections;

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
        messageItem = new MessageItem(
                creator, Collections.singletonList(singleHistory),
                singleHistory.getCreatedAt(), singleHistory.getCreatedAt()
        );
        messageTree = new MessageTree(
                forum, "TestTree", messageItem, MessagePriority.NORMAL
        );
    }

    @Test
    void testSaveMessageItem() {
        userDao.save(creator);
        messageDao.saveMessageItem(messageItem);
        assertNotEquals(0, messageItem.getId());
    }

    @Test
    void testGetMessageItemByHisId() {
        userDao.save(creator);
        messageDao.saveMessageItem(messageItem);

        final MessageItem selectedMessage = messageDao.getMessageById(messageItem.getId());
        assertEquals(messageItem, selectedMessage);
    }

    @Test
    void testGetRootMessageInTree() {
        userDao.save(creator);
        forumDao.save(forum);
        messageTreeDao.newMessageTree(messageTree);

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
    void testGetRootMessageInTreeWithComments() {
        final User user = new User(
                "otheruser", "user@gmail.com", "passwd"
        );
        userDao.save(creator);
        userDao.save(user);
        forumDao.save(forum);
        messageTreeDao.newMessageTree(messageTree);

        final HistoryItem historyItem1 = new HistoryItem(
                "body1", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem messageItem1 = new MessageItem(
                creator, messageTree, messageItem,
                Collections.singletonList(historyItem1),
                historyItem1.getCreatedAt(), historyItem1.getCreatedAt()
        );
        messageDao.saveMessageItem(messageItem1);

        final HistoryItem historyItem2 = new HistoryItem(
                "body2", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem messageItem2 = new MessageItem(
                user, messageTree, messageItem,
                Collections.singletonList(historyItem2),
                historyItem2.getCreatedAt(), historyItem2.getCreatedAt()
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
    void testDeleteMessageById() {
        userDao.save(creator);
        messageDao.saveMessageItem(messageItem);

        messageDao.deleteById(messageItem.getId());
        final MessageItem deletedMessage = messageDao.getMessageById(messageItem.getId());
        assertNull(deletedMessage);
    }
}