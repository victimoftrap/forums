package net.thumbtack.forums.controller;

import net.thumbtack.forums.service.StatisticService;
import net.thumbtack.forums.dto.responses.statistic.UserRatingDtoResponse;
import net.thumbtack.forums.dto.responses.statistic.UserRatingListDtoResponse;
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

    @Test
    void testGetUsersRatingsOnServer() throws Exception {
        final List<UserRatingDtoResponse> ratings = new ArrayList<>();
        ratings.add(new UserRatingDtoResponse(132, "Leo", 4.64));
        ratings.add(new UserRatingDtoResponse(137, "Donnie", 4.012));
        ratings.add(new UserRatingDtoResponse(152, "Raph", 3.5));
        ratings.add(new UserRatingDtoResponse(173, "Mike", 3.5));
        ratings.add(new UserRatingDtoResponse(1, "admin", 0.));

        final UserRatingListDtoResponse expectedResponse = new UserRatingListDtoResponse(ratings);
        when(mockStatisticService.getUsersRatings(eq(COOKIE_VALUE), eq(null), eq(0), eq(10)))
                .thenReturn(expectedResponse);

        final MvcResult result = mvc.perform(
                get("/api/statistics/users-ratings")
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
        ratings.add(new UserRatingDtoResponse(132, "Leo", 4.64));
        ratings.add(new UserRatingDtoResponse(137, "Donnie", 4.012));
        ratings.add(new UserRatingDtoResponse(152, "Raph", 3.5));
        ratings.add(new UserRatingDtoResponse(173, "Mike", 3.5));
        ratings.add(new UserRatingDtoResponse(1, "admin", 0.));

        final UserRatingListDtoResponse expectedResponse = new UserRatingListDtoResponse(ratings);
        when(mockStatisticService.getUsersRatings(eq(COOKIE_VALUE), eq(123), eq(0), eq(10)))
                .thenReturn(expectedResponse);

        final MvcResult result = mvc.perform(
                get("/api/statistics/users-ratings")
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
        ratings.add(new UserRatingDtoResponse(132, "Leo", 4.64));
        ratings.add(new UserRatingDtoResponse(137, "Donnie", 4.012));
        ratings.add(new UserRatingDtoResponse(152, "Raph", 3.5));
        ratings.add(new UserRatingDtoResponse(173, "Mike", 3.5));
        ratings.add(new UserRatingDtoResponse(1, "admin", 0.));

        final UserRatingListDtoResponse expectedResponse = new UserRatingListDtoResponse(ratings);
        when(mockStatisticService.getUsersRatings(anyString(), eq(null), eq(null), eq(null)))
                .thenReturn(expectedResponse);

        final MvcResult result = mvc.perform(
                get("/api/statistics/users-ratings")
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

    static Stream<Arguments> getUsersRatingsServiceExceptions() {
        return Stream.of(
                Arguments.arguments(ErrorCode.WRONG_SESSION_TOKEN),
                Arguments.arguments(ErrorCode.FORUM_NOT_FOUND)
        );
    }

    @ParameterizedTest
    @MethodSource("getUsersRatingsServiceExceptions")
    void testGetUsersRatings_exceptionInService_shouldReturnExceptionDto(ErrorCode errorCode) throws Exception {
        when(mockStatisticService.getUsersRatings(anyString(), eq(null), anyInt(), anyInt()))
                .thenThrow(new ServerException(errorCode));

        mvc.perform(
                get("/api/statistics/users-ratings")
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
                .andExpect(jsonPath("$.errors[0].field").doesNotExist())
                .andExpect(jsonPath("$.errors[0].message").exists());

        verify(mockStatisticService)
                .getUsersRatings(anyString(), eq(null), anyInt(), anyInt());
    }
}