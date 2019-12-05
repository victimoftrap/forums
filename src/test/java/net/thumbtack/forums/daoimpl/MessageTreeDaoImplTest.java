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
        messageItem = new MessageItem(
                creator, Collections.singletonList(historyItem),
                historyItem.getCreatedAt(), historyItem.getCreatedAt()
        );
        messageTree = new MessageTree(
                forum, "Subject", messageItem, MessagePriority.NORMAL
        );
    }

    @Test
    void testCreateMessageTree() {
        userDao.save(creator);
        forumDao.save(forum);

        messageTreeDao.newMessageTree(messageTree);
        assertAll(
                () -> assertNotEquals(0, messageTree.getId()),
                () -> assertNotEquals(0, messageItem.getId())
        );
    }

    @Test
    void testCreateTreeFromRegularMessage() {
        userDao.save(creator);
        forumDao.save(forum);
        messageTreeDao.newMessageTree(messageTree);

        final HistoryItem otherMessageHistory = new HistoryItem(
                "Czech Republic", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem otherMessage = new MessageItem(
                creator, messageTree, messageItem, Collections.singletonList(otherMessageHistory),
                otherMessageHistory.getCreatedAt(), otherMessageHistory.getCreatedAt()
        );
        messageDao.saveMessageItem(otherMessage);

        final MessageTree otherTree = new MessageTree(
                forum, "Eastern Europe", otherMessage, MessagePriority.NORMAL
        );
        messageTreeDao.newBranch(otherTree);

        final MessageItem selectedOtherMessage = messageDao.getMessageById(otherMessage.getId());
        assertAll(
                () -> assertEquals(otherMessage.getId(), selectedOtherMessage.getId()),
                () -> assertEquals(otherMessage.getOwner(), selectedOtherMessage.getOwner()),
                () -> assertEquals(otherMessage.getRating(), selectedOtherMessage.getRating()),
                () -> assertEquals(otherMessage.getCreatedAt(), selectedOtherMessage.getCreatedAt()),
                () -> assertEquals(otherMessage.getUpdatedAt(), selectedOtherMessage.getUpdatedAt()),
                () -> assertEquals(otherMessage.getChildrenComments(), selectedOtherMessage.getChildrenComments()),
                () -> assertNotEquals(otherMessage.getMessageTree(), selectedOtherMessage.getMessageTree()),
                () -> assertNotEquals(otherMessage.getParentMessage(), selectedOtherMessage.getParentMessage())
        );
    }

    @Test
    void testChangeTreePriority() {
        userDao.save(creator);
        forumDao.save(forum);
        messageTreeDao.newMessageTree(messageTree);

        messageTree.setPriority(MessagePriority.HIGH);
        messageTreeDao.changeBranchPriority(messageTree);

        final MessageItem message = messageDao.getMessageById(messageItem.getId());
        final MessageTree selectedTree = message.getMessageTree();

        assertEquals(messageTree.getPriority(), selectedTree.getPriority());
        assertEquals(messageTree.getId(), selectedTree.getId());
        assertEquals(messageTree.getRootMessage().getId(), selectedTree.getRootMessage().getId());
    }
}