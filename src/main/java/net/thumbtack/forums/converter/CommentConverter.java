package net.thumbtack.forums.converter;

import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.dto.responses.message.CommentInfoDtoResponse;

import java.util.List;
import java.util.ArrayList;

public class CommentConverter {
    public static CommentInfoDtoResponse commentToResponse(final MessageItem comment) {
        final List<String> history = HistoryConverter.historyToBodyHistoryList(comment.getHistory());

        return new CommentInfoDtoResponse(
                comment.getId(),
                comment.getOwner().getUsername(),
                history,
                comment.getCreatedAt(),
                comment.getAverageRating(),
                comment.getRatings().size(),
                commentListToResponse(comment.getChildrenComments())
        );
    }

    public static List<CommentInfoDtoResponse> commentListToResponse(List<MessageItem> comments) {
        final List<CommentInfoDtoResponse> commentsResponse = new ArrayList<>();

        for (final MessageItem aComment : comments) {
            commentsResponse.add(
                    commentToResponse(aComment)
            );
        }
        return commentsResponse;
    }
}
