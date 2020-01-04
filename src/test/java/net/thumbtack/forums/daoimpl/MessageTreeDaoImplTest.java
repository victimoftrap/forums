package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.model.*;
import net.thumbtack.forums.model.enums.*;
import net.thumbtack.forums.exception.ServerException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
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
                forum, "TestTree", null, MessagePriority.NORMAL,
                historyItem.getCreatedAt(),
                Arrays.asList(new Tag("Tag1"), new Tag("Tag2"), new Tag("Tag3"))
        );
        messageItem = new MessageItem(
                creator, messageTree, null,
                Collections.singletonList(historyItem),
                historyItem.getCreatedAt(), historyItem.getCreatedAt()
        );
        messageTree.setRootMessage(messageItem);
    }

    @Test
    void testCreateMessageTree() throws ServerException {
        userDao.save(creator);
        forumDao.save(forum);

        messageTreeDao.saveMessageTree(messageTree);
        assertNotEquals(0, messageItem.getId());
        assertNotEquals(0, messageTree.getId());
    }

    @Test
    void testCreateAndGetTreeFromComment() throws ServerException {
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
        assertEquals(comment.getAverageRating(), selectedNewTreeRootMessage.getAverageRating());
        assertEquals(comment.getCreatedAt(), selectedNewTreeRootMessage.getCreatedAt());
        assertEquals(comment.getUpdatedAt(), selectedNewTreeRootMessage.getUpdatedAt());
        assertEquals(comment.getChildrenComments(), selectedNewTreeRootMessage.getChildrenComments());

        final MessageTree tree = selectedNewTreeRootMessage.getMessageTree();
        assertNotEquals(messageTree.getId(), tree.getId());
        assertEquals(comment.getMessageTree().getId(), tree.getId());
        assertNull(selectedNewTreeRootMessage.getParentMessage());
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
    void testGetRootMessage_commentOrderAscAndDesc() throws ServerException {
        final User commentMaker = new User(
                "commentMaker", "user@gmail.com", "passwd"
        );
        userDao.save(creator);
        userDao.save(commentMaker);
        forumDao.save(forum);
        messageTreeDao.saveMessageTree(messageTree);

        final HistoryItem commentHistory1 = new HistoryItem(
                "comment body 1", MessageState.PUBLISHED,
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem commentItem1 = new MessageItem(
                creator, messageTree, messageItem,
                Collections.singletonList(commentHistory1),
                commentHistory1.getCreatedAt(), commentHistory1.getCreatedAt()
        );
        messageDao.saveMessageItem(commentItem1);

        final HistoryItem commentHistory2 = new HistoryItem(
                "comment body 2", MessageState.PUBLISHED,
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS)
                        .plus(1, ChronoUnit.HOURS)
        );
        final MessageItem commentItem2 = new MessageItem(
                commentMaker, messageTree, messageItem,
                Collections.singletonList(commentHistory2),
                commentHistory2.getCreatedAt(), commentHistory2.getCreatedAt()
        );
        messageDao.saveMessageItem(commentItem2);

        final HistoryItem commentHistory21 = new HistoryItem(
                "comment body 2_1", MessageState.UNPUBLISHED,
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS)
                        .plus(1, ChronoUnit.YEARS)
        );
        commentItem2.setHistory(Arrays.asList(commentHistory21, commentHistory2));
        messageHistoryDao.saveNewVersion(commentItem2);

        final MessageItem selectedRootDesc = messageTreeDao.getTreeRootMessage(
                messageItem.getId(),
                MessageOrder.DESC,
                false, true, true
        );
        assertEquals(messageItem.getId(), selectedRootDesc.getId());
        final List<MessageItem> selectedCommentsDesc = selectedRootDesc.getChildrenComments();
        assertEquals(2, selectedCommentsDesc.size());
        assertEquals(commentItem2.getId(), selectedCommentsDesc.get(0).getId());
        assertEquals(commentItem1.getId(), selectedCommentsDesc.get(1).getId());
        assertEquals(2, selectedCommentsDesc.get(0).getHistory().size());
        assertEquals(1, selectedCommentsDesc.get(1).getHistory().size());

        final MessageItem selectedRootAsc = messageTreeDao.getTreeRootMessage(
                messageItem.getId(),
                MessageOrder.ASC,
                false, true, true
        );
        assertEquals(messageItem.getId(), selectedRootDesc.getId());
        final List<MessageItem> selectedCommentsAsc = selectedRootAsc.getChildrenComments();
        assertEquals(2, selectedCommentsAsc.size());
        assertEquals(commentItem1.getId(), selectedCommentsAsc.get(0).getId());
        assertEquals(commentItem2.getId(), selectedCommentsAsc.get(1).getId());
        assertEquals(1, selectedCommentsAsc.get(0).getHistory().size());
        assertEquals(2, selectedCommentsAsc.get(1).getHistory().size());
    }

    @Test
    void testGetRootMessage_noComments() throws ServerException {
        final User commentMaker = new User(
                "commentMaker", "user@gmail.com", "passwd"
        );
        userDao.save(creator);
        userDao.save(commentMaker);
        forumDao.save(forum);
        messageTreeDao.saveMessageTree(messageTree);

        final HistoryItem commentHistory1 = new HistoryItem(
                "comment body 1", MessageState.PUBLISHED,
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem commentItem1 = new MessageItem(
                creator, messageTree, messageItem,
                Collections.singletonList(commentHistory1),
                commentHistory1.getCreatedAt(), commentHistory1.getCreatedAt()
        );
        messageDao.saveMessageItem(commentItem1);

        final HistoryItem commentHistory2 = new HistoryItem(
                "comment body 2", MessageState.PUBLISHED,
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS)
                        .plus(1, ChronoUnit.HOURS)
        );
        final MessageItem commentItem2 = new MessageItem(
                commentMaker, messageTree, messageItem,
                Collections.singletonList(commentHistory2),
                commentHistory2.getCreatedAt(), commentHistory2.getCreatedAt()
        );
        messageDao.saveMessageItem(commentItem2);

        final HistoryItem commentHistory21 = new HistoryItem(
                "comment body 2_1", MessageState.UNPUBLISHED,
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS)
                        .plus(1, ChronoUnit.YEARS)
        );
        commentItem2.setHistory(Arrays.asList(commentHistory21, commentHistory2));
        messageHistoryDao.saveNewVersion(commentItem2);

        final MessageItem selectedRoot = messageTreeDao.getTreeRootMessage(
                messageItem.getId(),
                MessageOrder.DESC,
                true, true, true
        );
        assertEquals(messageItem.getId(), selectedRoot.getId());
        final List<MessageItem> selectedComments = selectedRoot.getChildrenComments();
        assertEquals(0, selectedComments.size());
    }

    @Test
    void testGetRootMessage_onlyLatestHistory() throws ServerException {
        final User commentMaker = new User(
                "commentMaker", "user@gmail.com", "passwd"
        );
        userDao.save(creator);
        userDao.save(commentMaker);
        forumDao.save(forum);
        messageTreeDao.saveMessageTree(messageTree);

        final HistoryItem commentHistory1 = new HistoryItem(
                "comment body 1", MessageState.PUBLISHED,
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem commentItem1 = new MessageItem(
                creator, messageTree, messageItem,
                Collections.singletonList(commentHistory1),
                commentHistory1.getCreatedAt(), commentHistory1.getCreatedAt()
        );
        messageDao.saveMessageItem(commentItem1);

        final HistoryItem commentHistory2 = new HistoryItem(
                "comment body 2", MessageState.PUBLISHED,
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS)
                        .plus(1, ChronoUnit.HOURS)
        );
        final MessageItem commentItem2 = new MessageItem(
                commentMaker, messageTree, messageItem,
                Collections.singletonList(commentHistory2),
                commentHistory2.getCreatedAt(), commentHistory2.getCreatedAt()
        );
        messageDao.saveMessageItem(commentItem2);

        final HistoryItem commentHistory21 = new HistoryItem(
                "comment body 2_1", MessageState.UNPUBLISHED,
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS)
                        .plus(1, ChronoUnit.YEARS)
        );
        commentItem2.setHistory(Arrays.asList(commentHistory21, commentHistory2));
        messageHistoryDao.saveNewVersion(commentItem2);

        final MessageItem selectedRootDesc = messageTreeDao.getTreeRootMessage(
                messageItem.getId(),
                MessageOrder.DESC,
                false, false, true
        );
        assertEquals(messageItem.getId(), selectedRootDesc.getId());
        final List<MessageItem> selectedCommentsDesc = selectedRootDesc.getChildrenComments();
        assertEquals(2, selectedCommentsDesc.size());
        assertEquals(commentItem2.getId(), selectedCommentsDesc.get(0).getId());
        assertEquals(commentItem1.getId(), selectedCommentsDesc.get(1).getId());

        final List<HistoryItem> selectedComments1 = selectedCommentsDesc.get(0).getHistory();
        assertEquals(1, selectedComments1.size());
        assertTrue(selectedComments1.get(0).getBody().contains("[UNPUBLISHED]"));
        assertTrue(selectedComments1.get(0).getBody().contains(commentHistory21.getBody()));

        final List<HistoryItem> selectedComments2 = selectedCommentsDesc.get(1).getHistory();
        assertEquals(1, selectedComments2.size());
        assertTrue(selectedComments2.get(0).getBody().contains(commentHistory1.getBody()));
    }

    @Test
    void testGetRootMessage_onlyPublishedHistory() throws ServerException {
        final User commentMaker = new User(
                "commentMaker", "user@gmail.com", "passwd"
        );
        userDao.save(creator);
        userDao.save(commentMaker);
        forumDao.save(forum);
        messageTreeDao.saveMessageTree(messageTree);

        final HistoryItem commentHistory1 = new HistoryItem(
                "comment body 1", MessageState.PUBLISHED,
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem commentItem1 = new MessageItem(
                creator, messageTree, messageItem,
                Collections.singletonList(commentHistory1),
                commentHistory1.getCreatedAt(), commentHistory1.getCreatedAt()
        );
        messageDao.saveMessageItem(commentItem1);

        final HistoryItem commentHistory2 = new HistoryItem(
                "comment body 2", MessageState.PUBLISHED,
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS)
                        .plus(1, ChronoUnit.HOURS)
        );
        final MessageItem commentItem2 = new MessageItem(
                commentMaker, messageTree, messageItem,
                Collections.singletonList(commentHistory2),
                commentHistory2.getCreatedAt(), commentHistory2.getCreatedAt()
        );
        messageDao.saveMessageItem(commentItem2);

        final HistoryItem commentHistory21 = new HistoryItem(
                "comment body 2_1", MessageState.PUBLISHED,
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS)
                        .plus(1, ChronoUnit.YEARS)
        );
        commentItem2.setHistory(Arrays.asList(commentHistory21, commentHistory2));
        messageHistoryDao.saveNewVersion(commentItem2);

        final HistoryItem commentHistory22 = new HistoryItem(
                "comment body 2_2", MessageState.UNPUBLISHED,
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS)
                        .plus(2, ChronoUnit.YEARS)
        );
        commentItem2.setHistory(Arrays.asList(commentHistory22, commentHistory21, commentHistory2));
        messageHistoryDao.saveNewVersion(commentItem2);

        final MessageItem selectedRootDesc = messageTreeDao.getTreeRootMessage(
                messageItem.getId(),
                MessageOrder.DESC,
                false, true, false
        );
        assertEquals(messageItem.getId(), selectedRootDesc.getId());
        final List<MessageItem> selectedCommentsDesc = selectedRootDesc.getChildrenComments();
        assertEquals(2, selectedCommentsDesc.size());
        assertEquals(commentItem2.getId(), selectedCommentsDesc.get(0).getId());
        assertEquals(commentItem1.getId(), selectedCommentsDesc.get(1).getId());

        final List<HistoryItem> selectedComments1 = selectedCommentsDesc.get(0).getHistory();
        assertEquals(2, selectedComments1.size());
        assertTrue(selectedComments1.get(0).getBody().contains(commentHistory21.getBody()));
        assertTrue(selectedComments1.get(0).getBody().contains(commentHistory2.getBody()));

        final List<HistoryItem> selectedComments2 = selectedCommentsDesc.get(1).getHistory();
        assertEquals(1, selectedComments2.size());
        assertTrue(selectedComments2.get(0).getBody().contains(commentHistory1.getBody()));
    }

    @Test
    void testGetRootMessage_onlyLatestPublishedHistory() throws ServerException {
        final User commentMaker = new User(
                "commentMaker", "user@gmail.com", "passwd"
        );
        userDao.save(creator);
        userDao.save(commentMaker);
        forumDao.save(forum);
        messageTreeDao.saveMessageTree(messageTree);

        final HistoryItem commentHistory1 = new HistoryItem(
                "comment body 1", MessageState.PUBLISHED,
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem commentItem1 = new MessageItem(
                creator, messageTree, messageItem,
                Collections.singletonList(commentHistory1),
                commentHistory1.getCreatedAt(), commentHistory1.getCreatedAt()
        );
        messageDao.saveMessageItem(commentItem1);

        final HistoryItem commentHistory2 = new HistoryItem(
                "comment body 2", MessageState.PUBLISHED,
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS)
                        .plus(1, ChronoUnit.HOURS)
        );
        final MessageItem commentItem2 = new MessageItem(
                commentMaker, messageTree, messageItem,
                Collections.singletonList(commentHistory2),
                commentHistory2.getCreatedAt(), commentHistory2.getCreatedAt()
        );
        messageDao.saveMessageItem(commentItem2);

        final HistoryItem commentHistory21 = new HistoryItem(
                "comment body 2_1", MessageState.UNPUBLISHED,
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS)
                        .plus(1, ChronoUnit.YEARS)
        );
        commentItem2.setHistory(Arrays.asList(commentHistory21, commentHistory2));
        messageHistoryDao.saveNewVersion(commentItem2);

        final MessageItem selectedRootDesc = messageTreeDao.getTreeRootMessage(
                messageItem.getId(),
                MessageOrder.DESC,
                false, false, false
        );
        assertEquals(messageItem.getId(), selectedRootDesc.getId());
        final List<MessageItem> selectedCommentsDesc = selectedRootDesc.getChildrenComments();
        assertEquals(2, selectedCommentsDesc.size());
        assertEquals(commentItem2.getId(), selectedCommentsDesc.get(0).getId());
        assertEquals(commentItem1.getId(), selectedCommentsDesc.get(1).getId());

        final List<HistoryItem> selectedComments1 = selectedCommentsDesc.get(0).getHistory();
        assertEquals(1, selectedComments1.size());
        assertTrue(selectedComments1.get(0).getBody().contains(commentHistory2.getBody()));

        final List<HistoryItem> selectedComments2 = selectedCommentsDesc.get(1).getHistory();
        assertEquals(1, selectedComments2.size());
        assertTrue(selectedComments2.get(0).getBody().contains(commentHistory1.getBody()));
    }

    @Test
    void testChangeTreePriority() throws ServerException {
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
                () -> assertEquals(messageItem.getAverageRating(), message.getAverageRating()),
                () -> assertEquals(messageItem.getChildrenComments(), message.getChildrenComments()),
                () -> assertEquals(messageItem.getMessageTree().getId(), message.getMessageTree().getId()),
                () -> assertNull(messageItem.getParentMessage()),
                () -> assertEquals(messageItem.getParentMessage(), message.getParentMessage())
        );
//        assertEquals(messageTree, selectedTree); // stack overflow because of toString on fail nested objects
    }

    @Test
    void testDeleteMessageTreeById() throws ServerException {
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
    void testDeleteMessageTreeByRootMessageId() throws ServerException {
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