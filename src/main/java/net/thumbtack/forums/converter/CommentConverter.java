package net.thumbtack.forums.converter;

import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.dto.responses.message.CommentInfoDtoResponse;

import java.util.ArrayList;
import java.util.List;

public class CommentConverter {
    public static CommentInfoDtoResponse commentToCommentResponse(final MessageItem comment) {
        final List<String> history = HistoryConverter.historyToBodyHistoryList(comment.getHistory());

        return new CommentInfoDtoResponse(
                comment.getId(),
                comment.getOwner().getUsername(),
                history,
                comment.getCreatedAt(),
                comment.getAverageRating(),
                comment.getRatings().size(),
                new ArrayList<>()
        );
    }
}
