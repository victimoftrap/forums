package net.thumbtack.forums.service;

import net.thumbtack.forums.dto.responses.statistic.*;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.Forum;
import net.thumbtack.forums.model.enums.ForumType;
import net.thumbtack.forums.view.MessageRatingView;
import net.thumbtack.forums.view.UserRatingView;
import net.thumbtack.forums.dao.ForumDao;
import net.thumbtack.forums.dao.SessionDao;
import net.thumbtack.forums.dao.StatisticDao;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.configuration.ConstantsProperties;
import net.thumbtack.forums.configuration.ServerConfigurationProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class StatisticServiceTest {
    private StatisticDao mockStatisticDao;
    private SessionDao mockSessionDao;
    private ForumDao mockForumDao;
    private ServerConfigurationProperties mockServerConfigurationProperties;
    private ConstantsProperties mockConstantsProperties;
    private StatisticService statisticService;

    @BeforeEach
    void initMocks() {
        mockStatisticDao = mock(StatisticDao.class);
        mockSessionDao = mock(SessionDao.class);
        mockForumDao = mock(ForumDao.class);
        mockServerConfigurationProperties = mock(ServerConfigurationProperties.class);
        mockConstantsProperties = mock(ConstantsProperties.class);

        statisticService = new StatisticService(
                mockStatisticDao, mockSessionDao, mockForumDao,
                mockServerConfigurationProperties, mockConstantsProperties
        );
    }

    @Test
    void testGetMessagesCount_forumIdNotSend_shouldReturnCountInServer() throws ServerException {
        final User user = new User("user", "user@email.com", "v3rY$troNGPA$$");
        final int messagesCount = 1234;

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(user);
        when(mockStatisticDao.getMessagesCount())
                .thenReturn(messagesCount);

        final MessagesCountDtoResponse response = statisticService.getMessagesCount(
                "token", null
        );
        assertEquals(messagesCount, response.getMessagesCount());
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockStatisticDao)
                .getMessagesCount();

        verifyZeroInteractions(mockForumDao);
        verify(mockStatisticDao, never())
                .getMessagesCount(anyInt());
    }

    @Test
    void testGetMessagesCount_forumIdReceived_shouldReturnCountInThisForum() throws ServerException {
        final User user = new User("user", "user@email.com", "v3rY$troNGPA$$");
        final User forumOwner = new User("owner", "owner@email.com", "ownerpass");
        final Forum forum = new Forum(
                ForumType.MODERATED, forumOwner, "ForumName",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );

        final int forumId = 147;
        final int messagesCount = 45678;

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(user);
        when(mockForumDao.getById(eq(forumId)))
                .thenReturn(forum);
        when(mockStatisticDao.getMessagesCount(eq(forumId)))
                .thenReturn(messagesCount);

        final MessagesCountDtoResponse response = statisticService.getMessagesCount(
                "token", forumId
        );
        assertEquals(messagesCount, response.getMessagesCount());
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockForumDao)
                .getById(eq(forumId));
        verify(mockStatisticDao)
                .getMessagesCount(eq(forumId));

        verify(mockStatisticDao, never())
                .getMessagesCount();
    }

    @Test
    void testGetMessagesCount_userNotFoundByToken_shouldThrowException() throws ServerException {
        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(null);

        try {
            statisticService.getMessagesCount("token", null);
        } catch (ServerException se) {
            assertEquals(ErrorCode.NO_USER_SESSION, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());

        verifyZeroInteractions(mockForumDao);
        verifyZeroInteractions(mockStatisticDao);
    }

    @Test
    void testGetMessagesCount_forumNotFound_shouldThrowException() throws ServerException {
        final User user = new User("user", "user@email.com", "v3rY$troNGPA$$");
        final int forumId = 123;
        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(user);
        when(mockForumDao.getById(eq(forumId)))
                .thenReturn(null);

        try {
            statisticService.getMessagesCount("token", forumId);
        } catch (ServerException se) {
            assertEquals(ErrorCode.FORUM_NOT_FOUND, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockForumDao)
                .getById(eq(forumId));
        verifyZeroInteractions(mockStatisticDao);
    }

    @Test
    void testGetCommentsCount_forumIdNotSend_shouldReturnCountInServer() throws ServerException {
        final User user = new User("user", "user@email.com", "v3rY$troNGPA$$");
        final int commentsCount = 95123;

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(user);
        when(mockStatisticDao.getCommentsCount())
                .thenReturn(commentsCount);

        final CommentsCountDtoResponse response = statisticService.getCommentsCount(
                "token", null
        );
        assertEquals(commentsCount, response.getCommentsCount());
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockStatisticDao)
                .getCommentsCount();

        verifyZeroInteractions(mockForumDao);
        verify(mockStatisticDao, never())
                .getCommentsCount(anyInt());
    }

    @Test
    void testGetCommentsCount_forumIdReceived_shouldReturnCountInThisForum() throws ServerException {
        final User user = new User("user", "user@email.com", "v3rY$troNGPA$$");
        final User forumOwner = new User("owner", "owner@email.com", "ownerpass");
        final Forum forum = new Forum(
                ForumType.MODERATED, forumOwner, "ForumName",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );

        final int forumId = 147;
        final int commentsCount = 759153;

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(user);
        when(mockForumDao.getById(eq(forumId)))
                .thenReturn(forum);
        when(mockStatisticDao.getCommentsCount(eq(forumId)))
                .thenReturn(commentsCount);

        final CommentsCountDtoResponse response = statisticService.getCommentsCount(
                "token", forumId
        );
        assertEquals(commentsCount, response.getCommentsCount());
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockForumDao)
                .getById(eq(forumId));
        verify(mockStatisticDao)
                .getCommentsCount(eq(forumId));

        verify(mockStatisticDao, never())
                .getCommentsCount();
    }

    @Test
    void testGetCommentsCount_userNotFoundByToken_shouldThrowException() throws ServerException {
        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(null);

        try {
            statisticService.getCommentsCount("token", null);
        } catch (ServerException se) {
            assertEquals(ErrorCode.NO_USER_SESSION, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());

        verifyZeroInteractions(mockForumDao);
        verifyZeroInteractions(mockStatisticDao);
    }

    @Test
    void testGetCommentsCount_forumNotFound_shouldThrowException() throws ServerException {
        final User user = new User("user", "user@email.com", "v3rY$troNGPA$$");
        final int forumId = 123;
        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(user);
        when(mockForumDao.getById(eq(forumId)))
                .thenReturn(null);

        try {
            statisticService.getCommentsCount("token", forumId);
        } catch (ServerException se) {
            assertEquals(ErrorCode.FORUM_NOT_FOUND, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockForumDao)
                .getById(eq(forumId));
        verifyZeroInteractions(mockStatisticDao);
    }

    @Test
    void testGetMessagesRatings_forumIdNull_returnRatingsInAllServer() throws ServerException {
        final User requesterUser = new User("Test", "test@email.com", "testpass");

        final List<MessageRatingView> ratingViews = new ArrayList<>();
        ratingViews.add(new MessageRatingView(789, true, 4.7, 5));
        ratingViews.add(new MessageRatingView(456, false, 4.51, 4));
        ratingViews.add(new MessageRatingView(123, false, 3.751, 17));
        ratingViews.add(new MessageRatingView(9, true, 2, 1));
        ratingViews.add(new MessageRatingView(1, true, 0., 0));
        ratingViews.add(new MessageRatingView(1, false, 0., 0));

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(requesterUser);
        when(mockStatisticDao.getMessagesRatings(anyInt(), anyInt()))
                .thenReturn(ratingViews);

        final List<MessageRatingDtoResponse> response = new ArrayList<>();
        ratingViews.forEach(rv -> response.add(
                new MessageRatingDtoResponse(
                        rv.getMessageId(),
                        rv.isMessage() ? "message" : "comment",
                        rv.getRating(),
                        rv.getRated()
                )
        ));
        final MessageRatingListDtoResponse expectedResponse = new MessageRatingListDtoResponse(response);
        final MessageRatingListDtoResponse actualResponse = statisticService.getMessagesRatings(
                "token", null, 0, 300
        );
        assertEquals(expectedResponse, actualResponse);

        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockStatisticDao)
                .getMessagesRatings(anyInt(), anyInt());

        verifyZeroInteractions(mockForumDao);
        verify(mockStatisticDao, never())
                .getMessagesRatingsInForum(anyInt(), anyInt(), anyInt());
    }

    @Test
    void testGetMessagesRatings_forumIdNotNull_shouldReturnRatingsInForum() throws ServerException {
        final User requesterUser = new User("Test", "test@email.com", "testpass");
        final User forumOwner = new User("owner", "owner@email.com", "ownerpass");
        final Forum forum = new Forum(
                ForumType.MODERATED, forumOwner, "ForumName",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );

        final List<MessageRatingView> ratingViews = new ArrayList<>();
        ratingViews.add(new MessageRatingView(789, true, 4.7, 5));
        ratingViews.add(new MessageRatingView(456, false, 4.51, 4));
        ratingViews.add(new MessageRatingView(123, false, 3.751, 17));
        ratingViews.add(new MessageRatingView(9, true, 2, 1));
        ratingViews.add(new MessageRatingView(1, true, 0., 0));
        ratingViews.add(new MessageRatingView(1, false, 0., 0));

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(requesterUser);
        when(mockForumDao.getById(anyInt()))
                .thenReturn(forum);
        when(mockStatisticDao.getMessagesRatingsInForum(anyInt(), anyInt(), anyInt()))
                .thenReturn(ratingViews);

        final List<MessageRatingDtoResponse> response = new ArrayList<>();
        ratingViews.forEach(rv -> response.add(
                new MessageRatingDtoResponse(
                        rv.getMessageId(),
                        rv.isMessage() ? "message" : "comment",
                        rv.getRating(),
                        rv.getRated()
                )
        ));
        final MessageRatingListDtoResponse expectedResponse = new MessageRatingListDtoResponse(response);
        final MessageRatingListDtoResponse actualResponse = statisticService.getMessagesRatings(
                "token", 132, 0, 300
        );
        assertEquals(expectedResponse, actualResponse);

        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockForumDao)
                .getById(anyInt());
        verify(mockStatisticDao)
                .getMessagesRatingsInForum(anyInt(), anyInt(), anyInt());
        verify(mockStatisticDao, never())
                .getMessagesRatings(anyInt(), anyInt());
    }

    @Test
    void testGetMessagesRatings_offsetAndLimitNotReceived_shouldApplyDefaultSettings() throws ServerException {
        final User requesterUser = new User("Test", "test@email.com", "testpass");
        final User forumOwner = new User("owner", "owner@email.com", "ownerpass");
        final Forum forum = new Forum(
                ForumType.MODERATED, forumOwner, "ForumName",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );

        final List<MessageRatingView> ratingViews = new ArrayList<>();
        ratingViews.add(new MessageRatingView(789, true, 4.7, 5));
        ratingViews.add(new MessageRatingView(456, false, 4.51, 4));
        ratingViews.add(new MessageRatingView(123, false, 3.751, 17));
        ratingViews.add(new MessageRatingView(9, true, 2, 1));
        ratingViews.add(new MessageRatingView(1, true, 0., 0));
        ratingViews.add(new MessageRatingView(1, false, 0., 0));

        final int defaultOffset = 0;
        final int defaultLimit = 3000;
        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(requesterUser);
        when(mockForumDao.getById(anyInt()))
                .thenReturn(forum);
        when(mockConstantsProperties.getDefaultOffset())
                .thenReturn(defaultOffset);
        when(mockConstantsProperties.getDefaultLimit())
                .thenReturn(defaultLimit);
        when(mockStatisticDao.getMessagesRatingsInForum(anyInt(), anyInt(), anyInt()))
                .thenReturn(ratingViews);

        final List<MessageRatingDtoResponse> response = new ArrayList<>();
        ratingViews.forEach(rv -> response.add(
                new MessageRatingDtoResponse(
                        rv.getMessageId(),
                        rv.isMessage() ? "message" : "comment",
                        rv.getRating(),
                        rv.getRated()
                )
        ));
        final MessageRatingListDtoResponse expectedResponse = new MessageRatingListDtoResponse(response);
        final MessageRatingListDtoResponse actualResponse = statisticService.getMessagesRatings(
                "token", 951, null, null
        );

        assertEquals(expectedResponse, actualResponse);
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockForumDao)
                .getById(anyInt());
        verify(mockConstantsProperties)
                .getDefaultOffset();
        verify(mockConstantsProperties)
                .getDefaultLimit();
        verify(mockStatisticDao)
                .getMessagesRatingsInForum(anyInt(), anyInt(), anyInt());
        verify(mockStatisticDao, never())
                .getMessagesRatings(anyInt(), anyInt());
    }

    @Test
    void testGetMessagesRatings_noRatingsFound_returnEmptyList() throws ServerException {
        final User requesterUser = new User("Test", "test@email.com", "testpass");

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(requesterUser);
        when(mockStatisticDao.getMessagesRatings(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        final MessageRatingListDtoResponse expectedResponse = new MessageRatingListDtoResponse(
                Collections.emptyList()
        );
        final MessageRatingListDtoResponse actualResponse = statisticService.getMessagesRatings(
                "token", null, 0, 300
        );
        assertEquals(expectedResponse, actualResponse);

        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockStatisticDao)
                .getMessagesRatings(anyInt(), anyInt());
        verify(mockStatisticDao, never())
                .getMessagesRatingsInForum(anyInt(), anyInt(), anyInt());
    }

    @Test
    void testGetMessagesRatings_userNotFoundByToken_shouldThrowException() throws ServerException {
        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(null);

        try {
            statisticService.getMessagesRatings(
                    "token", null, 0, 300
            );
        } catch (ServerException se) {
            assertEquals(ErrorCode.NO_USER_SESSION, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verifyZeroInteractions(mockStatisticDao);
    }

    @Test
    void testGetMessagesRatings_forumNotFound_shouldThrowException() throws ServerException {
        final User requesterUser = new User("Test", "test@email.com", "testpass");

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(requesterUser);
        when(mockForumDao.getById(anyInt()))
                .thenReturn(null);

        try {
            statisticService.getMessagesRatings(
                    "token", 951, 0, 300
            );
        } catch (ServerException se) {
            assertEquals(ErrorCode.FORUM_NOT_FOUND, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockForumDao)
                .getById(anyInt());
        verifyZeroInteractions(mockStatisticDao);
    }

    @Test
    void testGetUsersRatings_forumIdNull_returnRatingsInAllServer() throws ServerException {
        final User requesterUser = new User("Test", "test@email.com", "testpass");

        final List<UserRatingView> ratingViews = new ArrayList<>();
        ratingViews.add(new UserRatingView(789, "user3", 4.5, 2));
        ratingViews.add(new UserRatingView(456, "user2", 3.1415, 9));
        ratingViews.add(new UserRatingView(123, "user1", 2.71828, 18));
        ratingViews.add(new UserRatingView(1, "admin", 0., 0));

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(requesterUser);
        when(mockStatisticDao.getUsersRatings(anyInt(), anyInt()))
                .thenReturn(ratingViews);

        final List<UserRatingDtoResponse> response = new ArrayList<>();
        ratingViews.forEach(rv -> response.add(
                new UserRatingDtoResponse(
                        rv.getUserId(), rv.getUsername(), rv.getRating(), rv.getRated()
                )
        ));
        final UserRatingListDtoResponse expectedResponse = new UserRatingListDtoResponse(response);
        final UserRatingListDtoResponse actualResponse = statisticService.getUsersRatings(
                "token", null, 0, 300
        );
        assertEquals(expectedResponse, actualResponse);

        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockStatisticDao)
                .getUsersRatings(anyInt(), anyInt());

        verifyZeroInteractions(mockForumDao);
        verify(mockStatisticDao, never())
                .getUsersRatingsInForum(anyInt(), anyInt(), anyInt());
    }

    @Test
    void testGetUsersRatings_forumIdNotNull_shouldReturnRatingsInForum() throws ServerException {
        final User requesterUser = new User("Test", "test@email.com", "testpass");
        final User forumOwner = new User("owner", "owner@email.com", "ownerpass");
        final Forum forum = new Forum(
                ForumType.MODERATED, forumOwner, "ForumName",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );

        final List<UserRatingView> ratingViews = new ArrayList<>();
        ratingViews.add(new UserRatingView(789, "user3", 4.5, 2));
        ratingViews.add(new UserRatingView(456, "user2", 3.1415, 9));
        ratingViews.add(new UserRatingView(123, "user1", 2.71828, 18));
        ratingViews.add(new UserRatingView(1, "admin", 0., 0));

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(requesterUser);
        when(mockForumDao.getById(anyInt()))
                .thenReturn(forum);
        when(mockStatisticDao.getUsersRatingsInForum(anyInt(), anyInt(), anyInt()))
                .thenReturn(ratingViews);

        final List<UserRatingDtoResponse> response = new ArrayList<>();
        ratingViews.forEach(rv -> response.add(
                new UserRatingDtoResponse(
                        rv.getUserId(), rv.getUsername(), rv.getRating(), rv.getRated()
                )
        ));
        final UserRatingListDtoResponse expectedResponse = new UserRatingListDtoResponse(response);
        final UserRatingListDtoResponse actualResponse = statisticService.getUsersRatings(
                "token", 951, 0, 300
        );

        assertEquals(expectedResponse, actualResponse);
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockForumDao)
                .getById(anyInt());
        verify(mockStatisticDao)
                .getUsersRatingsInForum(anyInt(), anyInt(), anyInt());
        verify(mockStatisticDao, never())
                .getUsersRatings(anyInt(), anyInt());
    }

    @Test
    void testGetUsersRatings_offsetAndLimitNotReceived_shouldApplyDefaultSettings() throws ServerException {
        final User requesterUser = new User("Test", "test@email.com", "testpass");
        final User forumOwner = new User("owner", "owner@email.com", "ownerpass");
        final Forum forum = new Forum(
                ForumType.MODERATED, forumOwner, "ForumName",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );

        final List<UserRatingView> ratingViews = new ArrayList<>();
        ratingViews.add(new UserRatingView(789, "user3", 4.5, 2));
        ratingViews.add(new UserRatingView(456, "user2", 3.1415, 9));
        ratingViews.add(new UserRatingView(123, "user1", 2.71828, 18));
        ratingViews.add(new UserRatingView(1, "admin", 0., 0));

        final int defaultOffset = 0;
        final int defaultLimit = 3000;
        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(requesterUser);
        when(mockForumDao.getById(anyInt()))
                .thenReturn(forum);
        when(mockConstantsProperties.getDefaultOffset())
                .thenReturn(defaultOffset);
        when(mockConstantsProperties.getDefaultLimit())
                .thenReturn(defaultLimit);
        when(mockStatisticDao.getUsersRatingsInForum(anyInt(), anyInt(), anyInt()))
                .thenReturn(ratingViews);

        final List<UserRatingDtoResponse> response = new ArrayList<>();
        ratingViews.forEach(rv -> response.add(
                new UserRatingDtoResponse(
                        rv.getUserId(), rv.getUsername(), rv.getRating(), rv.getRated()
                )
        ));
        final UserRatingListDtoResponse expectedResponse = new UserRatingListDtoResponse(response);
        final UserRatingListDtoResponse actualResponse = statisticService.getUsersRatings(
                "token", 951, null, null
        );

        assertEquals(expectedResponse, actualResponse);
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockForumDao)
                .getById(anyInt());
        verify(mockConstantsProperties)
                .getDefaultOffset();
        verify(mockConstantsProperties)
                .getDefaultLimit();
        verify(mockStatisticDao)
                .getUsersRatingsInForum(anyInt(), anyInt(), anyInt());
        verify(mockStatisticDao, never())
                .getUsersRatings(anyInt(), anyInt());
    }

    @Test
    void testGetUsersRatings_noRatingsFound_returnEmptyList() throws ServerException {
        final User requesterUser = new User("Test", "test@email.com", "testpass");

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(requesterUser);
        when(mockStatisticDao.getUsersRatings(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        final UserRatingListDtoResponse expectedResponse = new UserRatingListDtoResponse(
                Collections.emptyList()
        );
        final UserRatingListDtoResponse actualResponse = statisticService.getUsersRatings(
                "token", null, 0, 300
        );
        assertEquals(expectedResponse, actualResponse);

        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockStatisticDao)
                .getUsersRatings(anyInt(), anyInt());
        verify(mockStatisticDao, never())
                .getUsersRatingsInForum(anyInt(), anyInt(), anyInt());
    }

    @Test
    void testGetUsersRatings_userNotFoundByToken_shouldThrowException() throws ServerException {
        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(null);

        try {
            statisticService.getUsersRatings("token", null, 0, 300);
        } catch (ServerException se) {
            assertEquals(ErrorCode.NO_USER_SESSION, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verifyZeroInteractions(mockStatisticDao);
    }

    @Test
    void testGetUsersRatings_forumNotFound_shouldThrowException() throws ServerException {
        final User requesterUser = new User("Test", "test@email.com", "testpass");

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(requesterUser);
        when(mockForumDao.getById(anyInt()))
                .thenReturn(null);

        try {
            statisticService.getUsersRatings(
                    "token", 951, 0, 300
            );
        } catch (ServerException se) {
            assertEquals(ErrorCode.FORUM_NOT_FOUND, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockForumDao)
                .getById(anyInt());
        verifyZeroInteractions(mockStatisticDao);
    }
}