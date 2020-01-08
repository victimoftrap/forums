package net.thumbtack.forums.converter;

import net.thumbtack.forums.view.MessageRatingView;
import net.thumbtack.forums.view.UserRatingView;
import net.thumbtack.forums.dto.responses.statistic.MessageRatingListDtoResponse;
import net.thumbtack.forums.dto.responses.statistic.MessageRatingDtoResponse;
import net.thumbtack.forums.dto.responses.statistic.UserRatingDtoResponse;
import net.thumbtack.forums.dto.responses.statistic.UserRatingListDtoResponse;

import java.util.List;
import java.util.ArrayList;

public class StatisticsConverter {
    public static MessageRatingListDtoResponse messagesRatingsToResponse(final List<MessageRatingView> ratings) {
        final List<MessageRatingDtoResponse> ratingsResponse = new ArrayList<>();
        ratings.forEach(mrv -> ratingsResponse.add(
                new MessageRatingDtoResponse(
                        mrv.getMessageId(),
                        mrv.isMessage() ? "message" : "comment",
                        mrv.getRating(),
                        mrv.getRated()
                ))
        );
        return new MessageRatingListDtoResponse(ratingsResponse);
    }

    public static UserRatingListDtoResponse usersRatingsToResponse(final List<UserRatingView> ratings) {
        final List<UserRatingDtoResponse> ratingsResponse = new ArrayList<>();
        ratings.forEach(urv -> ratingsResponse.add(
                new UserRatingDtoResponse(
                        urv.getUserId(),
                        urv.getUsername(),
                        urv.getRating(),
                        urv.getRated()
                ))
        );
        return new UserRatingListDtoResponse(ratingsResponse);
    }
}
