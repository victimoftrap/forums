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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessageTreeDaoImplTest extends DaoTestEnvironment {
    private static User creator;
    private static Forum forum;
    private static MessageTree messageTree;
    private static MessageItem messageItem;
    private static HistoryItem historyItem;

    @BeforeAll
    static void createModels() {
        creator = new User(
                UserRole.USER,
                "shermental", "shermental@gmail.com", "passwd",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        forum = new Forum(
                ForumType.UNMODERATED, creator, "Europe",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        historyItem = new HistoryItem(
                "Main message", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        messageTree = new MessageTree(
                forum, "TestTree", null, MessagePriority.NORMAL
        );
        messageItem = new MessageItem(
                creator, messageTree, null,
                Collections.singletonList(historyItem),
                historyItem.getCreatedAt(), historyItem.getCreatedAt()
        );
        messageTree.setRootMessage(messageItem);
    }

    @Test
    void testCreateMessageTree() {
        userDao.save(creator);
        forumDao.save(forum);

        messageTreeDao.saveMessageTree(messageTree);
        assertAll(
                () -> assertNotEquals(0, messageTree.getId()),
                () -> assertNotEquals(0, messageItem.getId())
        );
    }

    @Test
    void testCreateAndGetTreeFromComment() {
        userDao.save(creator);
        forumDao.save(forum);
        messageTreeDao.saveMessageTree(messageTree);

        final HistoryItem commentHistory = new HistoryItem(
                "Czech Republic", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem comment = new MessageItem(
                creator, messageTree, messageItem, Collections.singletonList(commentHistory),
                commentHistory.getCreatedAt(), commentHistory.getCreatedAt()
        );
        messageDao.saveMessageItem(comment);

        final MessageTree newMessageTree = new MessageTree(
                forum, "Eastern Europe", comment, MessagePriority.NORMAL
        );
        comment.setParentMessage(null);
        comment.setMessageTree(newMessageTree);
        messageTreeDao.newBranch(newMessageTree);

        final MessageItem selectedNewTreeRootMessage = messageDao.getMessageById(comment.getId());
        assertEquals(comment.getId(), selectedNewTreeRootMessage.getId());
        assertEquals(comment.getOwner(), selectedNewTreeRootMessage.getOwner());
        assertEquals(comment.getRating(), selectedNewTreeRootMessage.getRating());
        assertEquals(comment.getCreatedAt(), selectedNewTreeRootMessage.getCreatedAt());
        assertEquals(comment.getUpdatedAt(), selectedNewTreeRootMessage.getUpdatedAt());
        assertEquals(comment.getChildrenComments(), selectedNewTreeRootMessage.getChildrenComments());

        assertNotEquals(messageTree.getId(), selectedNewTreeRootMessage.getMessageTree().getId());
        assertEquals(comment.getMessageTree().getId(), selectedNewTreeRootMessage.getMessageTree().getId());
        assertNull(selectedNewTreeRootMessage.getParentMessage());
    }

    @Test
    void testGetRootMessageInTreeWithComments() {
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
                historyItem1.getCreatedAt(), historyItem1.getCreatedAt()
        );
        messageDao.saveMessageItem(messageItem1);

        final HistoryItem historyItem2 = new HistoryItem(
                "comment body 2", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem messageItem2 = new MessageItem(
                commentMaker, messageTree, messageItem,
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
    void testChangeTreePriority() {
        userDao.save(creator);
        forumDao.save(forum);
        messageTreeDao.saveMessageTree(messageTree);

        messageTree.setPriority(MessagePriority.HIGH);
        messageTreeDao.changeBranchPriority(messageTree);

        final MessageItem message = messageDao.getMessageById(messageItem.getId());
        assertAll(
                () -> assertEquals(messageItem.getId(), message.getId()),
                () -> assertEquals(messageItem.getOwner(), message.getOwner()),
                () -> assertEquals(messageItem.getParentMessage(), message.getParentMessage()),
                () -> assertEquals(messageItem.getHistory(), message.getHistory()),
                () -> assertEquals(messageItem.getUpdatedAt(), message.getUpdatedAt()),
                () -> assertEquals(messageItem.getCreatedAt(), message.getCreatedAt()),
                () -> assertEquals(messageItem.getRating(), message.getRating()),
                () -> assertEquals(messageItem.getChildrenComments(), message.getChildrenComments()),
                () -> assertEquals(messageItem.getMessageTree().getId(), message.getMessageTree().getId()),
                () -> assertNull(messageItem.getParentMessage()),
                () -> assertEquals(messageItem.getParentMessage(), message.getParentMessage())
        );
//        assertEquals(messageTree, selectedTree); // stack overflow because of toString on fail nested objects
    }

    @Test
    void testDeleteMessageTreeById() {
        userDao.save(creator);
        forumDao.save(forum);
        messageTreeDao.saveMessageTree(messageTree);

        final MessageItem selectedRootBeforeDeletion = messageDao.getMessageById(messageItem.getId());
        assertNotNull(selectedRootBeforeDeletion);

        messageTreeDao.deleteTreeById(messageItem.getMessageTree().getId());
        final MessageItem selectedRootAfterDeletion = messageDao.getMessageById(messageItem.getId());
        assertNull(selectedRootAfterDeletion);
    }

    @Test
    void testDeleteMessageTreeByRootMessageId() {
        userDao.save(creator);
        forumDao.save(forum);
        messageTreeDao.saveMessageTree(messageTree);

        final MessageItem selectedRootBeforeDeletion = messageDao.getMessageById(messageItem.getId());
        assertNotNull(selectedRootBeforeDeletion);

        messageTreeDao.deleteTreeByRootMessageId(messageItem.getId());
        final MessageItem selectedRootAfterDeletion = messageDao.getMessageById(messageItem.getId());
        assertNull(selectedRootAfterDeletion);
    }
}