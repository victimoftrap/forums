package net.thumbtack.forums.controller;

import net.thumbtack.forums.dto.responses.statistic.*;
import net.thumbtack.forums.service.StatisticService;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import javax.servlet.http.Cookie;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = StatisticsController.class)
class StatisticsControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private StatisticService mockStatisticService;

    private final String COOKIE_NAME = "JAVASESSIONID";
    private final String COOKIE_VALUE = UUID.randomUUID().toString();

    static Stream<Arguments> statisticsServiceExceptions() {
        return Stream.of(
                Arguments.arguments(ErrorCode.WRONG_SESSION_TOKEN),
                Arguments.arguments(ErrorCode.FORUM_NOT_FOUND)
        );
    }

    @Test
    void testGetMessagesCountOnServer() throws Exception {
        final MessagesCountDtoResponse response = new MessagesCountDtoResponse(2016);
        when(mockStatisticService.getMessagesCount(anyString(), eq(null)))
                .thenReturn(response);

        mvc.perform(
                get("/api/statistics/messages/count")
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.messagesCount").value(response.getMessagesCount()));

        verify(mockStatisticService)
                .getMessagesCount(anyString(), eq(null));
    }

    @Test
    void testGetMessagesCountOnForum() throws Exception {
        final int forumId = 123;
        final MessagesCountDtoResponse response = new MessagesCountDtoResponse(48151623);
        when(mockStatisticService.getMessagesCount(anyString(), eq(forumId)))
                .thenReturn(response);

        mvc.perform(
                get("/api/statistics/messages/count")
                        .param("forum-id", String.valueOf(forumId))
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.messagesCount").value(response.getMessagesCount()));

        verify(mockStatisticService)
                .getMessagesCount(anyString(), eq(forumId));
    }

    @ParameterizedTest
    @MethodSource("statisticsServiceExceptions")
    void testGetMessagesCount_exceptionInService_shouldReturnExceptionDto(ErrorCode errorCode) throws Exception {
        when(mockStatisticService.getMessagesCount(anyString(), anyInt()))
                .thenThrow(new ServerException(errorCode));

        mvc.perform(
                get("/api/statistics/messages/count")
                        .param("forum-id", "123")
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(errorCode.name()))
                .andExpect(jsonPath("$.errors[0].field").value(errorCode.getErrorCauseField()))
                .andExpect(jsonPath("$.errors[0].message").value(errorCode.getMessage()));

        verify(mockStatisticService)
                .getMessagesCount(anyString(), anyInt());
    }

    @Test
    void testGetCommentsCountOnServer() throws Exception {
        final CommentsCountDtoResponse response = new CommentsCountDtoResponse(202016);
        when(mockStatisticService.getCommentsCount(anyString(), eq(null)))
                .thenReturn(response);

        mvc.perform(
                get("/api/statistics/comments/count")
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.commentsCount").value(response.getCommentsCount()));

        verify(mockStatisticService)
                .getCommentsCount(anyString(), eq(null));
    }

    @Test
    void testGetCommentsCountOnForum() throws Exception {
        final int forumId = 123;
        final CommentsCountDtoResponse response = new CommentsCountDtoResponse(1202016);
        when(mockStatisticService.getCommentsCount(anyString(), eq(forumId)))
                .thenReturn(response);

        mvc.perform(
                get("/api/statistics/comments/count")
                        .param("forum-id", String.valueOf(forumId))
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.commentsCount").value(response.getCommentsCount()));

        verify(mockStatisticService)
                .getCommentsCount(anyString(), eq(forumId));
    }

    @ParameterizedTest
    @MethodSource("statisticsServiceExceptions")
    void testGetCommentsCount_exceptionInService_shouldReturnExceptionDto(ErrorCode errorCode) throws Exception {
        final int forumId = 123;
        when(mockStatisticService.getCommentsCount(anyString(), eq(forumId)))
                .thenThrow(new ServerException(errorCode));

        mvc.perform(
                get("/api/statistics/comments/count")
                        .param("forum-id", String.valueOf(forumId))
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(errorCode.name()))
                .andExpect(jsonPath("$.errors[0].field").value(errorCode.getErrorCauseField()))
                .andExpect(jsonPath("$.errors[0].message").value(errorCode.getMessage()));

        verify(mockStatisticService)
                .getCommentsCount(anyString(), eq(forumId));
    }

    @Test
    void testGetMessagesRatingsOnServer() throws Exception {
        final List<MessageRatingDtoResponse> ratings = new ArrayList<>();
        ratings.add(new MessageRatingDtoResponse(132, "message", 5, 1));
        ratings.add(new MessageRatingDtoResponse(147, "message", 4.78, 6));
        ratings.add(new MessageRatingDtoResponse(154, "comment", 4.4, 12));
        ratings.add(new MessageRatingDtoResponse(101, "message", 4.1, 8));
        ratings.add(new MessageRatingDtoResponse(204, "comment", 3.88, 7));
        ratings.add(new MessageRatingDtoResponse(113, "message", 3.5, 4));
        ratings.add(new MessageRatingDtoResponse(172, "comment", 3.5, 4));

        final MessageRatingListDtoResponse expectedResponse = new MessageRatingListDtoResponse(ratings);
        when(mockStatisticService.getMessagesRatings(eq(COOKIE_VALUE), eq(null), eq(0), eq(10)))
                .thenReturn(expectedResponse);

        final MvcResult result = mvc.perform(
                get("/api/statistics/messages/ratings")
                        .param("offset", "0")
                        .param("limit", "10")
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.messagesRatings", hasSize(7)))
                .andReturn();

        final MessageRatingListDtoResponse actualResponse = mapper.readValue(
                result.getResponse().getContentAsString(),
                MessageRatingListDtoResponse.class
        );
        assertEquals(expectedResponse, actualResponse);

        verify(mockStatisticService)
                .getMessagesRatings(eq(COOKIE_VALUE), eq(null), eq(0), eq(10));
    }

    @Test
    void testGetMessagesRatingsOnForum() throws Exception {
        final List<MessageRatingDtoResponse> ratings = new ArrayList<>();
        ratings.add(new MessageRatingDtoResponse(132, "message", 5, 1));
        ratings.add(new MessageRatingDtoResponse(147, "message", 4.78, 6));
        ratings.add(new MessageRatingDtoResponse(113, "message", 3.5, 4));
        ratings.add(new MessageRatingDtoResponse(172, "comment", 3.5, 4));
        final MessageRatingListDtoResponse expectedResponse = new MessageRatingListDtoResponse(ratings);

        final int forumId = 123;
        when(mockStatisticService.getMessagesRatings(eq(COOKIE_VALUE), eq(forumId), eq(0), eq(10)))
                .thenReturn(expectedResponse);

        final MvcResult result = mvc.perform(
                get("/api/statistics/messages/ratings")
                        .param("forum-id", String.valueOf(forumId))
                        .param("offset", "0")
                        .param("limit", "10")
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.messagesRatings", hasSize(4)))
                .andReturn();

        final MessageRatingListDtoResponse actualResponse = mapper.readValue(
                result.getResponse().getContentAsString(),
                MessageRatingListDtoResponse.class
        );
        assertEquals(expectedResponse, actualResponse);

        verify(mockStatisticService)
                .getMessagesRatings(eq(COOKIE_VALUE), eq(forumId), eq(0), eq(10));
    }

    @ParameterizedTest
    @MethodSource("statisticsServiceExceptions")
    void testGetMessagesRatings_exceptionInService_shouldReturnExceptionDto(ErrorCode errorCode) throws Exception {
        when(mockStatisticService.getMessagesRatings(anyString(), eq(null), anyInt(), anyInt()))
                .thenThrow(new ServerException(errorCode));

        mvc.perform(
                get("/api/statistics/messages/ratings")
                        .param("offset", "0")
                        .param("limit", "10")
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(errorCode.name()))
                .andExpect(jsonPath("$.errors[0].field").value(errorCode.getErrorCauseField()))
                .andExpect(jsonPath("$.errors[0].message").value(errorCode.getMessage()));

        verify(mockStatisticService)
                .getMessagesRatings(anyString(), eq(null), anyInt(), anyInt());
    }

    @Test
    void testGetUsersRatingsOnServer() throws Exception {
        final List<UserRatingDtoResponse> ratings = new ArrayList<>();
        ratings.add(new UserRatingDtoResponse(132, "Leo", 4.64, 5));
        ratings.add(new UserRatingDtoResponse(137, "Donnie", 4.012, 6));
        ratings.add(new UserRatingDtoResponse(152, "Raph", 3.5, 4));
        ratings.add(new UserRatingDtoResponse(173, "Mike", 3.5, 4));
        ratings.add(new UserRatingDtoResponse(1, "admin", 0., 0));

        final UserRatingListDtoResponse expectedResponse = new UserRatingListDtoResponse(ratings);
        when(mockStatisticService.getUsersRatings(eq(COOKIE_VALUE), eq(null), eq(0), eq(10)))
                .thenReturn(expectedResponse);

        final MvcResult result = mvc.perform(
                get("/api/statistics/users/ratings")
                        .param("offset", "0")
                        .param("limit", "10")
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.usersRatings", hasSize(5)))
                .andReturn();

        final UserRatingListDtoResponse actualResponse = mapper.readValue(
                result.getResponse().getContentAsString(),
                UserRatingListDtoResponse.class
        );
        assertEquals(expectedResponse, actualResponse);

        verify(mockStatisticService)
                .getUsersRatings(eq(COOKIE_VALUE), eq(null), eq(0), eq(10));
    }

    @Test
    void testGetUsersRatingsOnForum() throws Exception {
        final List<UserRatingDtoResponse> ratings = new ArrayList<>();
        ratings.add(new UserRatingDtoResponse(132, "Leo", 4.64, 5));
        ratings.add(new UserRatingDtoResponse(137, "Donnie", 4.012, 6));
        ratings.add(new UserRatingDtoResponse(152, "Raph", 3.5, 4));
        ratings.add(new UserRatingDtoResponse(173, "Mike", 3.5, 4));
        ratings.add(new UserRatingDtoResponse(1, "admin", 0., 0));

        final UserRatingListDtoResponse expectedResponse = new UserRatingListDtoResponse(ratings);
        when(mockStatisticService.getUsersRatings(eq(COOKIE_VALUE), eq(123), eq(0), eq(10)))
                .thenReturn(expectedResponse);

        final MvcResult result = mvc.perform(
                get("/api/statistics/users/ratings")
                        .param("forum-id", "123")
                        .param("offset", "0")
                        .param("limit", "10")
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.usersRatings", hasSize(5)))
                .andReturn();

        final UserRatingListDtoResponse actualResponse = mapper.readValue(
                result.getResponse().getContentAsString(),
                UserRatingListDtoResponse.class
        );
        assertEquals(expectedResponse, actualResponse);

        verify(mockStatisticService)
                .getUsersRatings(eq(COOKIE_VALUE), eq(123), eq(0), eq(10));
    }

    @Test
    void testGetUsersRatingsOnServer_noOffsetAndLimitInParams_shouldApplyDefaultInService() throws Exception {
        final List<UserRatingDtoResponse> ratings = new ArrayList<>();
        ratings.add(new UserRatingDtoResponse(132, "Leo", 4.64, 5));
        ratings.add(new UserRatingDtoResponse(137, "Donnie", 4.012, 6));
        ratings.add(new UserRatingDtoResponse(152, "Raph", 3.5, 4));
        ratings.add(new UserRatingDtoResponse(173, "Mike", 3.5, 4));
        ratings.add(new UserRatingDtoResponse(1, "admin", 0., 0));

        final UserRatingListDtoResponse expectedResponse = new UserRatingListDtoResponse(ratings);
        when(mockStatisticService.getUsersRatings(anyString(), eq(null), eq(null), eq(null)))
                .thenReturn(expectedResponse);

        final MvcResult result = mvc.perform(
                get("/api/statistics/users/ratings")
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.usersRatings", hasSize(5)))
                .andReturn();

        final UserRatingListDtoResponse actualResponse = mapper.readValue(
                result.getResponse().getContentAsString(),
                UserRatingListDtoResponse.class
        );
        assertEquals(expectedResponse, actualResponse);

        verify(mockStatisticService)
                .getUsersRatings(anyString(), eq(null), eq(null), eq(null));
    }

    @ParameterizedTest
    @MethodSource("statisticsServiceExceptions")
    void testGetUsersRatings_exceptionInService_shouldReturnExceptionDto(ErrorCode errorCode) throws Exception {
        when(mockStatisticService.getUsersRatings(anyString(), eq(null), anyInt(), anyInt()))
                .thenThrow(new ServerException(errorCode));

        mvc.perform(
                get("/api/statistics/users/ratings")
                        .param("offset", "0")
                        .param("limit", "10")
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(errorCode.name()))
                .andExpect(jsonPath("$.errors[0].field").value(errorCode.getErrorCauseField()))
                .andExpect(jsonPath("$.errors[0].message").value(errorCode.getMessage()));

        verify(mockStatisticService)
                .getUsersRatings(anyString(), eq(null), anyInt(), anyInt());
    }
}