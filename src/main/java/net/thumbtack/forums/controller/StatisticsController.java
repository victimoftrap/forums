package net.thumbtack.forums.controller;

import net.thumbtack.forums.service.StatisticService;
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
            value = "/users-ratings",
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
}
