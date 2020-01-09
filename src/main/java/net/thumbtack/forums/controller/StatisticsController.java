package net.thumbtack.forums.controller;

import net.thumbtack.forums.service.StatisticService;
import net.thumbtack.forums.dto.responses.statistic.MessagesCountDtoResponse;
import net.thumbtack.forums.dto.responses.statistic.CommentsCountDtoResponse;
import net.thumbtack.forums.dto.responses.statistic.MessageRatingListDtoResponse;
import net.thumbtack.forums.dto.responses.statistic.UserRatingListDtoResponse;
import net.thumbtack.forums.exception.ServerException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    private final StatisticService statisticService;
    private final String COOKIE_NAME = "JAVASESSIONID";

    @Autowired
    public StatisticsController(final StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @GetMapping(
            value = "/messages/ratings",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MessageRatingListDtoResponse> getMessagesRatings(
            @CookieValue(value = COOKIE_NAME) String token,
            @RequestParam(value = "forum-id", required = false) Integer forumId,
            @RequestParam(value = "offset", required = false) Integer offset,
            @RequestParam(value = "limit", required = false) Integer limit
    ) throws ServerException {
        return ResponseEntity.ok(
                statisticService.getMessagesRatings(token, forumId, offset, limit)
        );
    }

    @GetMapping(
            value = "/users/ratings",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserRatingListDtoResponse> getUsersRatings(
            @CookieValue(value = COOKIE_NAME) String token,
            @RequestParam(value = "forum-id", required = false) Integer forumId,
            @RequestParam(value = "offset", required = false) Integer offset,
            @RequestParam(value = "limit", required = false) Integer limit
    ) throws ServerException {
        return ResponseEntity.ok(
                statisticService.getUsersRatings(token, forumId, offset, limit)
        );
    }

    @GetMapping(
            value = "/messages/count",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MessagesCountDtoResponse> getMessagesCount(
            @CookieValue(value = COOKIE_NAME) String token,
            @RequestParam(value = "forum-id", required = false) Integer forumId
    ) throws ServerException {
        return ResponseEntity
                .ok()
                .body(statisticService.getMessagesCount(token, forumId));
    }

    @GetMapping(
            value = "/comments/count",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<CommentsCountDtoResponse> getCommentsCount(
            @CookieValue(value = COOKIE_NAME) String token,
            @RequestParam(value = "forum-id", required = false) Integer forumId
    ) throws ServerException {
        return ResponseEntity
                .ok()
                .body(statisticService.getCommentsCount(token, forumId));
    }
}
