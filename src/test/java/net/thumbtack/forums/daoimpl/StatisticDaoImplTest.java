package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.model.*;
import net.thumbtack.forums.model.enums.ForumType;
import net.thumbtack.forums.model.enums.MessageState;
import net.thumbtack.forums.model.enums.MessagePriority;
import net.thumbtack.forums.view.MessageRatingView;
import net.thumbtack.forums.view.UserRatingView;
import net.thumbtack.forums.exception.ServerException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StatisticDaoImplTest extends DaoTestEnvironment {
    private static User userLeo;
    private static User userRaph;
    private static User userDonnie;
    private static User userMike;
    private static Forum forum1;
    private static Forum forum2;

    private static MessageItem message1Leo;
    private static MessageItem comment1Donnie;
    private static MessageItem comment2Raph;
    private static MessageItem message2Raph;
    private static MessageItem comment3Leo;

    @BeforeEach
    void createModels() throws ServerException {
        userLeo = new User("Leonardo", "leonardo@mail.com", "leopass");
        userRaph = new User("Raphael", "raphael@mail.com", "raphpass");
        userDonnie = new User("Donatello", "donatello@mail.com", "donpass");
        userMike = new User("Michelangelo", "michelangelo@mail.com", "mikepass");

        forum1 = new Forum(
                ForumType.UNMODERATED, userLeo, "FORUM #1",
                LocalDateTime
                        .now()
                        .truncatedTo(ChronoUnit.SECONDS)
        );
        forum2 = new Forum(
                ForumType.MODERATED, userRaph, "FORUM #2",
                LocalDateTime
                        .now()
                        .plus(1, ChronoUnit.WEEKS)
                        .truncatedTo(ChronoUnit.SECONDS)
        );

        final HistoryItem messageHistory1 = new HistoryItem(
                "MAIN #1", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageTree messageTree1 = new MessageTree(
                forum1, "SUBJECT #1", null,
                MessagePriority.NORMAL,
                messageHistory1.getCreatedAt(),
                Arrays.asList(new Tag("TAG#1"), new Tag("TAG#2"))
        );
        message1Leo = new MessageItem(
                userLeo, messageTree1, null,
                Collections.singletonList(messageHistory1),
                messageHistory1.getCreatedAt()
        );
        messageTree1.setRootMessage(message1Leo);

        final HistoryItem commentHistory11 = new HistoryItem(
                "COMMENT #1", MessageState.PUBLISHED,
                LocalDateTime.now()
                        .plus(1, ChronoUnit.HOURS)
                        .truncatedTo(ChronoUnit.SECONDS)
        );
        comment1Donnie = new MessageItem(
                userDonnie, messageTree1, message1Leo,
                Collections.singletonList(commentHistory11),
                commentHistory11.getCreatedAt()
        );

        final HistoryItem commentHistory12 = new HistoryItem(
                "COMMENT #2", MessageState.PUBLISHED,
                LocalDateTime.now()
                        .plus(1, ChronoUnit.DAYS)
                        .truncatedTo(ChronoUnit.SECONDS)
        );
        comment2Raph = new MessageItem(
                userRaph, messageTree1, message1Leo,
                Collections.singletonList(commentHistory12),
                commentHistory12.getCreatedAt()
        );

        final HistoryItem messageHistory2 = new HistoryItem(
                "MAIN #2", MessageState.PUBLISHED,
                LocalDateTime.now().plus(2, ChronoUnit.WEEKS).truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageTree messageTree2 = new MessageTree(
                forum2, "SUBJECT #2", null,
                MessagePriority.HIGH,
                messageHistory2.getCreatedAt(),
                Arrays.asList(new Tag("TAG#2"), new Tag("TAG#3"))
        );
        message2Raph = new MessageItem(
                userRaph, messageTree2, null,
                Collections.singletonList(messageHistory2),
                messageHistory2.getCreatedAt()
        );
        messageTree2.setRootMessage(message2Raph);

        final HistoryItem commentHistory21 = new HistoryItem(
                "COMMENT #3", MessageState.PUBLISHED,
                LocalDateTime.now()
                        .plus(1, ChronoUnit.DAYS)
                        .truncatedTo(ChronoUnit.SECONDS)
        );
        comment3Leo = new MessageItem(
                userLeo, messageTree2, message2Raph,
                Collections.singletonList(commentHistory21),
                commentHistory21.getCreatedAt()
        );

        userDao.save(userLeo);
        userDao.save(userRaph);
        userDao.save(userDonnie);
        userDao.save(userMike);

        forumDao.save(forum1);
        forumDao.save(forum2);

        messageTreeDao.saveMessageTree(messageTree1);
        messageDao.saveMessageItem(comment1Donnie);
        messageDao.saveMessageItem(comment2Raph);
        messageTreeDao.saveMessageTree(messageTree2);
        messageDao.saveMessageItem(comment3Leo);
    }

    @Test
    void testGetMessagesRatingsInServer() throws ServerException {
        ratingDao.upsertRating(message1Leo, userRaph, 2);
        ratingDao.upsertRating(message1Leo, userDonnie, 5);
        ratingDao.upsertRating(message1Leo, userMike, 5);

        ratingDao.upsertRating(message2Raph, userRaph, 2);
        ratingDao.upsertRating(message2Raph, userLeo, 5);
        ratingDao.upsertRating(message2Raph, userDonnie, 4);
        ratingDao.upsertRating(message2Raph, userMike, 3);

        ratingDao.upsertRating(comment1Donnie, userLeo, 4);

        ratingDao.upsertRating(comment3Leo, userRaph, 4);
        ratingDao.upsertRating(comment3Leo, userDonnie, 3);
        ratingDao.upsertRating(comment3Leo, userMike, 2);

        final List<MessageRatingView> expectedRatingViews = new ArrayList<>();
        expectedRatingViews.add(new MessageRatingView(
                message1Leo.getId(), true, 4
        ));
        expectedRatingViews.add(new MessageRatingView(
                comment1Donnie.getId(), false, 4
        ));
        expectedRatingViews.add(new MessageRatingView(
                message2Raph.getId(), true, 3.5
        ));
        expectedRatingViews.add(new MessageRatingView(
                comment3Leo.getId(), false, 3
        ));
        expectedRatingViews.add(new MessageRatingView(
                comment2Raph.getId(), false, 0
        ));

        final List<MessageRatingView> actualRatingViews = statisticDao.getMessagesRatings(0, 300);
        assertNotNull(actualRatingViews);
        assertEquals(expectedRatingViews, actualRatingViews);
    }

    @Test
    void testGetMessagesRatingsInServerInForum() throws ServerException {
        ratingDao.upsertRating(message1Leo, userRaph, 2);
        ratingDao.upsertRating(message1Leo, userDonnie, 5);
        ratingDao.upsertRating(message1Leo, userMike, 5);

        ratingDao.upsertRating(message2Raph, userRaph, 2);
        ratingDao.upsertRating(message2Raph, userLeo, 5);
        ratingDao.upsertRating(message2Raph, userDonnie, 4);
        ratingDao.upsertRating(message2Raph, userMike, 3);

        ratingDao.upsertRating(comment1Donnie, userLeo, 4);

        ratingDao.upsertRating(comment3Leo, userRaph, 4);
        ratingDao.upsertRating(comment3Leo, userDonnie, 3);
        ratingDao.upsertRating(comment3Leo, userMike, 2);

        final List<MessageRatingView> expectedRatingViews1 = new ArrayList<>();
        expectedRatingViews1.add(new MessageRatingView(
                message1Leo.getId(), true, 4
        ));
        expectedRatingViews1.add(new MessageRatingView(
                comment1Donnie.getId(), false, 4
        ));
        expectedRatingViews1.add(new MessageRatingView(
                comment2Raph.getId(), false, 0
        ));

        final List<MessageRatingView> actualRatingViews1 = statisticDao.getMessagesRatingsInForum(
                forum1.getId(), 0, 300
        );
        assertNotNull(actualRatingViews1);
        assertEquals(expectedRatingViews1, actualRatingViews1);

        final List<MessageRatingView> expectedRatingViews2 = new ArrayList<>();
        expectedRatingViews2.add(new MessageRatingView(
                message2Raph.getId(), true, 3.5
        ));
        expectedRatingViews2.add(new MessageRatingView(
                comment3Leo.getId(), false, 3
        ));

        final List<MessageRatingView> actualRatingViews2 = statisticDao.getMessagesRatingsInForum(
                forum2.getId(), 0, 300
        );
        assertNotNull(actualRatingViews2);
        assertEquals(expectedRatingViews2, actualRatingViews2);
    }

    @Test
    void testGetMessagesRatingsInServerInForum_noMessagesRated_shouldReturnViewListWithZeros() throws ServerException {
        final List<MessageRatingView> expectedRatingViews1 = new ArrayList<>();
        expectedRatingViews1.add(new MessageRatingView(
                message1Leo.getId(), true, 0.
        ));
        expectedRatingViews1.add(new MessageRatingView(
                comment1Donnie.getId(), false, 0.
        ));
        expectedRatingViews1.add(new MessageRatingView(
                comment2Raph.getId(), false, 0.
        ));

        final List<MessageRatingView> actualRatingViews1 = statisticDao.getMessagesRatingsInForum(
                forum1.getId(), 0, 300
        );
        assertNotNull(actualRatingViews1);
        assertEquals(expectedRatingViews1, actualRatingViews1);

        final List<MessageRatingView> expectedRatingViews2 = new ArrayList<>();
        expectedRatingViews2.add(new MessageRatingView(
                message2Raph.getId(), true, 0.
        ));
        expectedRatingViews2.add(new MessageRatingView(
                comment3Leo.getId(), false, 0.
        ));

        final List<MessageRatingView> actualRatingViews2 = statisticDao.getMessagesRatingsInForum(
                forum2.getId(), 0, 300
        );
        assertNotNull(actualRatingViews2);
        assertEquals(expectedRatingViews2, actualRatingViews2);
    }

    @Test
    void testGetMessagesRatingsInServerInForum_noMessagesInForum_shouldReturnViewListWithZeros() throws ServerException {
        final Forum forum3 = new Forum(
                ForumType.UNMODERATED, userMike, "FORUM #3",
                LocalDateTime
                        .now()
                        .truncatedTo(ChronoUnit.SECONDS)
        );
        forumDao.save(forum3);

        final List<MessageRatingView> actualRatingViews = statisticDao.getMessagesRatingsInForum(
                forum3.getId(), 0, 300
        );
        assertNotNull(actualRatingViews);
        assertEquals(Collections.emptyList(), actualRatingViews);
    }

    @Test
    void testGetMessagesRatingsInServerInForum_forumNotFound_shouldReturnEmptyViewList() throws ServerException {
        final List<MessageRatingView> actualRatingViews1 = statisticDao.getMessagesRatingsInForum(
                Integer.MAX_VALUE, 0, 300
        );
        assertNotNull(actualRatingViews1);
        assertEquals(Collections.emptyList(), actualRatingViews1);
    }

    @Test
    void testGetUsersRatingsInServer() throws ServerException {
        ratingDao.upsertRating(message1Leo, userRaph, 2);
        ratingDao.upsertRating(message1Leo, userDonnie, 4);
        ratingDao.upsertRating(message1Leo, userMike, 5);

        ratingDao.upsertRating(message2Raph, userRaph, 2);
        ratingDao.upsertRating(message2Raph, userLeo, 4);
        ratingDao.upsertRating(message2Raph, userDonnie, 4);
        ratingDao.upsertRating(message2Raph, userMike, 3);

        ratingDao.upsertRating(comment1Donnie, userLeo, 5);

        ratingDao.upsertRating(comment3Leo, userRaph, 4);
        ratingDao.upsertRating(comment3Leo, userDonnie, 5);
        ratingDao.upsertRating(comment3Leo, userMike, 2);

        final List<UserRatingView> expectedRatingViews = new ArrayList<>();
        expectedRatingViews.add(new UserRatingView(
                userDonnie.getId(), userDonnie.getUsername(), 5
        ));
        expectedRatingViews.add(new UserRatingView(
                userLeo.getId(), userLeo.getUsername(), 3.6667
        ));
        expectedRatingViews.add(new UserRatingView(
                userRaph.getId(), userRaph.getUsername(), 3.25
        ));
        expectedRatingViews.add(new UserRatingView(
               1, "admin", 0.
        ));
        expectedRatingViews.add(new UserRatingView(
                userMike.getId(), userMike.getUsername(), 0.
        ));

        final List<UserRatingView> actualRatingViews = statisticDao.getUsersRatings(0, 300);
        assertNotNull(actualRatingViews);
        assertEquals(expectedRatingViews, actualRatingViews);
    }

    @Test
    void testGetUsersRatingsInServer_paginatedResult() throws ServerException {
        ratingDao.upsertRating(message1Leo, userRaph, 2);
        ratingDao.upsertRating(message1Leo, userDonnie, 4);
        ratingDao.upsertRating(message1Leo, userMike, 5);

        ratingDao.upsertRating(message2Raph, userRaph, 2);
        ratingDao.upsertRating(message2Raph, userLeo, 4);
        ratingDao.upsertRating(message2Raph, userDonnie, 4);
        ratingDao.upsertRating(message2Raph, userMike, 3);

        ratingDao.upsertRating(comment1Donnie, userLeo, 5);

        ratingDao.upsertRating(comment3Leo, userRaph, 4);
        ratingDao.upsertRating(comment3Leo, userDonnie, 5);
        ratingDao.upsertRating(comment3Leo, userMike, 2);

        final List<UserRatingView> expectedRatingViews1 = new ArrayList<>();
        expectedRatingViews1.add(new UserRatingView(
                userDonnie.getId(), userDonnie.getUsername(), 5
        ));
        expectedRatingViews1.add(new UserRatingView(
                userLeo.getId(), userLeo.getUsername(), 3.6667
        ));

        final List<UserRatingView> actualRatingViews1 = statisticDao.getUsersRatings(0, 2);
        assertNotNull(actualRatingViews1);
        assertEquals(expectedRatingViews1, actualRatingViews1);

        final List<UserRatingView> expectedRatingViews2 = new ArrayList<>();
        expectedRatingViews2.add(new UserRatingView(
                userLeo.getId(), userLeo.getUsername(), 3.6667
        ));
        expectedRatingViews2.add(new UserRatingView(
                userRaph.getId(), userRaph.getUsername(), 3.25
        ));
        expectedRatingViews2.add(new UserRatingView(
                1, "admin", 0.
        ));

        final List<UserRatingView> actualRatingViews2 = statisticDao.getUsersRatings(1, 3);
        assertNotNull(actualRatingViews2);
        assertEquals(expectedRatingViews2, actualRatingViews2);
    }

    @Test
    void testGetUsersRatingsInForum() throws ServerException {
        ratingDao.upsertRating(message1Leo, userRaph, 2);
        ratingDao.upsertRating(message1Leo, userDonnie, 4);
        ratingDao.upsertRating(message1Leo, userMike, 5);

        ratingDao.upsertRating(message2Raph, userRaph, 2);
        ratingDao.upsertRating(message2Raph, userLeo, 4);
        ratingDao.upsertRating(message2Raph, userDonnie, 4);
        ratingDao.upsertRating(message2Raph, userMike, 3);

        ratingDao.upsertRating(comment1Donnie, userLeo, 5);

        ratingDao.upsertRating(comment3Leo, userRaph, 4);
        ratingDao.upsertRating(comment3Leo, userDonnie, 5);
        ratingDao.upsertRating(comment3Leo, userMike, 2);

        final List<UserRatingView> expectedRatingViews1 = new ArrayList<>();
        expectedRatingViews1.add(new UserRatingView(
                userDonnie.getId(), userDonnie.getUsername(), 5
        ));
        expectedRatingViews1.add(new UserRatingView(
                userLeo.getId(), userLeo.getUsername(), 3.6667
        ));
        expectedRatingViews1.add(new UserRatingView(
                userRaph.getId(), userRaph.getUsername(), 0.
        ));
        final List<UserRatingView> actualRatingViews1 = statisticDao.getUsersRatingsInForum(
                forum1.getId(), 0, 300
        );
        assertNotNull(actualRatingViews1);
        assertEquals(expectedRatingViews1, actualRatingViews1);

        final List<UserRatingView> expectedRatingViews2 = new ArrayList<>();
        expectedRatingViews2.add(new UserRatingView(
                userLeo.getId(), userLeo.getUsername(), 3.6667
        ));
        expectedRatingViews2.add(new UserRatingView(
                userRaph.getId(), userRaph.getUsername(), 3.25
        ));

        final List<UserRatingView> actualRatingViews2 = statisticDao.getUsersRatingsInForum(
                forum2.getId(), 0, 300
        );
        assertNotNull(actualRatingViews2);
        assertEquals(expectedRatingViews2, actualRatingViews2);
    }

    @Test
    void testGetUsersRatingsInForum_noMessagesRated_shouldReturnViewListWithZeros() throws ServerException {
        final List<UserRatingView> expectedRatingViews = new ArrayList<>();
        expectedRatingViews.add(new UserRatingView(
                userLeo.getId(), userLeo.getUsername(), 0.
        ));
        expectedRatingViews.add(new UserRatingView(
                userRaph.getId(), userRaph.getUsername(), 0.
        ));
        expectedRatingViews.add(new UserRatingView(
                userDonnie.getId(), userDonnie.getUsername(), 0.
        ));

        final List<UserRatingView> actualRatingViews = statisticDao.getUsersRatingsInForum(
                forum1.getId(), 0, 300
        );
        assertNotNull(actualRatingViews);
        assertEquals(expectedRatingViews, actualRatingViews);
    }

    @Test
    void testGetUsersRatingsInForum_noMessagesInForum_shouldReturnViewListWithZeros() throws ServerException {
        final Forum forum3 = new Forum(
                ForumType.UNMODERATED, userMike, "FORUM #3",
                LocalDateTime
                        .now()
                        .truncatedTo(ChronoUnit.SECONDS)
        );
        forumDao.save(forum3);

        final List<UserRatingView> actualRatingViews = statisticDao.getUsersRatingsInForum(
                forum3.getId(), 0, 300
        );
        assertNotNull(actualRatingViews);
        assertEquals(Collections.emptyList(), actualRatingViews);
    }

    @Test
    void testGetUsersRatingsInForum_forumNotFound_shouldReturnEmptyViewList() throws ServerException {
        final List<UserRatingView> actualRatingViews = statisticDao.getUsersRatingsInForum(
                Integer.MAX_VALUE, 0, 300
        );
        assertNotNull(actualRatingViews);
        assertEquals(Collections.emptyList(), actualRatingViews);
    }
}