package net.thumbtack.forums.converter;

import net.thumbtack.forums.view.UserRatingView;
import net.thumbtack.forums.dto.responses.statistic.UserRatingDtoResponse;
import net.thumbtack.forums.dto.responses.statistic.UserRatingListDtoResponse;

import java.util.ArrayList;
import java.util.List;

public class StatisticsConverter {
    public static UserRatingListDtoResponse usersRatingsToResponse(final List<UserRatingView> ratings) {
        final List<UserRatingDtoResponse> ratingsResponse = new ArrayList<>();
        ratings.forEach(urv -> ratingsResponse.add(
                new UserRatingDtoResponse(urv.getUserId(),
                        urv.getUsername(),
                        urv.getUserRating()
                ))
        );
        return new UserRatingListDtoResponse(ratingsResponse);
    }
}
