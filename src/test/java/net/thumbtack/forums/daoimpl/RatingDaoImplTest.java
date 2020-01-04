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
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class RatingDaoImplTest extends DaoTestEnvironment {
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
                "1st body", MessageState.UNPUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
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
    void testInsertRatingViaRateMethod() throws ServerException {
        userDao.save(creator);
        forumDao.save(forum);
        messageTreeDao.saveMessageTree(messageTree);

        final int rating = 5;
        final User otherUser = new User(
                "otherUser", "other@email.com", "passwd"
        );
        userDao.save(otherUser);
        ratingDao.rate(messageItem, otherUser, rating);

        final MessageItem message = messageDao.getMessageById(messageItem.getId());
        assertEquals(rating, message.getAverageRating());
    }

    @Test
    void testInsertRatingViaUpsertRatingMethod() throws ServerException {
        userDao.save(creator);
        forumDao.save(forum);
        messageTreeDao.saveMessageTree(messageTree);

        final int rating = 5;
        final User otherUser = new User(
                "otherUser", "other@email.com", "passwd"
        );
        userDao.save(otherUser);
        ratingDao.upsertRating(messageItem, otherUser, rating);

        final MessageItem message = messageDao.getMessageById(messageItem.getId());
        assertEquals(rating, message.getAverageRating());
    }

    @Test
    void testChangeRatingViaChangeRatingMethod() throws ServerException {
        userDao.save(creator);
        forumDao.save(forum);
        messageTreeDao.saveMessageTree(messageTree);

        final int rating = 5;
        final int newRating = 1;
        final User otherUser = new User(
                "otherUser", "other@email.com", "passwd"
        );
        userDao.save(otherUser);
        ratingDao.upsertRating(messageItem, otherUser, rating);
        ratingDao.upsertRating(messageItem, otherUser, newRating);

        final MessageItem message = messageDao.getMessageById(messageItem.getId());
        assertEquals(newRating, message.getAverageRating());
        assertNotEquals(rating, message.getAverageRating());
    }

    @Test
    void testChangeRatingViaUpsertMethod() throws ServerException {
        userDao.save(creator);
        forumDao.save(forum);
        messageTreeDao.saveMessageTree(messageTree);

        final int rating = 5;
        final int newRating = 1;
        final User otherUser = new User(
                "otherUser", "other@email.com", "passwd"
        );
        userDao.save(otherUser);
        ratingDao.upsertRating(messageItem, otherUser, rating);
        ratingDao.changeRating(messageItem, otherUser, newRating);

        final MessageItem message = messageDao.getMessageById(messageItem.getId());
        assertEquals(newRating, message.getAverageRating());
        assertNotEquals(rating, message.getAverageRating());
    }

    @Test
    void testDeleteRating() throws ServerException {
        userDao.save(creator);
        forumDao.save(forum);
        messageTreeDao.saveMessageTree(messageTree);

        final int rating = 5;
        final User otherUser = new User(
                "otherUser", "other@email.com", "passwd"
        );
        userDao.save(otherUser);
        ratingDao.upsertRating(messageItem, otherUser, rating);
        final MessageItem messageBeforeDeletingRating = messageDao.getMessageById(messageItem.getId());
        assertEquals(rating, messageBeforeDeletingRating.getAverageRating());

        ratingDao.deleteRate(messageItem, otherUser);
        final MessageItem messageAfterDeletingRating = messageDao.getMessageById(messageItem.getId());
        assertEquals(0, messageAfterDeletingRating.getAverageRating());
    }

    @Test
    void testGetMessageRating() throws ServerException {
        userDao.save(creator);
        forumDao.save(forum);
        messageTreeDao.saveMessageTree(messageTree);

        final int creatorUserRating = 4;
        ratingDao.upsertRating(messageItem, creator, creatorUserRating);

        final int otherUserRating = 5;
        final User otherUser = new User(
                "otherUser", "other@email.com", "passwd"
        );
        userDao.save(otherUser);
        ratingDao.upsertRating(messageItem, otherUser, otherUserRating);

        final int anotherUserRating = 5;
        final User anotherUser = new User(
                "anotherUser", "another@stanford.edu", "stanford"
        );
        userDao.save(anotherUser);
        ratingDao.upsertRating(messageItem, anotherUser, anotherUserRating);

        final double resultRating = ratingDao.getMessageRating(messageItem);
        assertEquals(14.0 / 3, resultRating, 1e-4);
    }
}