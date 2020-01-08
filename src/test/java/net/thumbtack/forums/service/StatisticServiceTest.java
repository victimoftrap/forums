package net.thumbtack.forums.service;

import net.thumbtack.forums.configuration.ConstantsProperties;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.Forum;
import net.thumbtack.forums.model.enums.ForumType;
import net.thumbtack.forums.view.UserRatingView;
import net.thumbtack.forums.dao.ForumDao;
import net.thumbtack.forums.dao.SessionDao;
import net.thumbtack.forums.dao.StatisticDao;
import net.thumbtack.forums.dto.responses.statistic.UserRatingDtoResponse;
import net.thumbtack.forums.dto.responses.statistic.UserRatingListDtoResponse;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;
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
    void testGetUsersRatings_forumIdNull_returnRatingsInAllServer() throws ServerException {
        final User requesterUser = new User("Test", "test@email.com", "testpass");

        final List<UserRatingView> ratingViews = new ArrayList<>();
        ratingViews.add(new UserRatingView(789, "user3", 4.5));
        ratingViews.add(new UserRatingView(456, "user2", 3.1415));
        ratingViews.add(new UserRatingView(123, "user1", 2.71828));
        ratingViews.add(new UserRatingView(1, "admin", 0.));

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(requesterUser);
        when(mockStatisticDao.getUsersRatings(anyInt(), anyInt()))
                .thenReturn(ratingViews);

        final List<UserRatingDtoResponse> response = new ArrayList<>();
        ratingViews.forEach(rv -> response.add(
                new UserRatingDtoResponse(rv.getUserId(), rv.getUsername(), rv.getUserRating())
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
        ratingViews.add(new UserRatingView(789, "user3", 4.5));
        ratingViews.add(new UserRatingView(456, "user2", 3.1415));
        ratingViews.add(new UserRatingView(123, "user1", 2.71828));
        ratingViews.add(new UserRatingView(1, "admin", 0.));

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(requesterUser);
        when(mockForumDao.getById(anyInt()))
                .thenReturn(forum);
        when(mockStatisticDao.getUsersRatingsInForum(anyInt(), anyInt(), anyInt()))
                .thenReturn(ratingViews);

        final List<UserRatingDtoResponse> response = new ArrayList<>();
        ratingViews.forEach(rv -> response.add(
                new UserRatingDtoResponse(rv.getUserId(), rv.getUsername(), rv.getUserRating())
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
        ratingViews.add(new UserRatingView(789, "user3", 4.5));
        ratingViews.add(new UserRatingView(456, "user2", 3.1415));
        ratingViews.add(new UserRatingView(123, "user1", 2.71828));
        ratingViews.add(new UserRatingView(1, "admin", 0.));

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
                new UserRatingDtoResponse(rv.getUserId(), rv.getUsername(), rv.getUserRating())
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
            assertEquals(ErrorCode.WRONG_SESSION_TOKEN, se.getErrorCode());
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