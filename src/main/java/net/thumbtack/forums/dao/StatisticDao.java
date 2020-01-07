package net.thumbtack.forums.dao;

import net.thumbtack.forums.view.MessageRatingView;
import net.thumbtack.forums.view.UserRatingView;
import net.thumbtack.forums.exception.ServerException;

import java.util.List;

public interface StatisticDao {
    List<MessageRatingView> getMessagesRatings(
            int offset, int limit
    ) throws ServerException;

    List<MessageRatingView> getMessagesRatingsInForum(
            int forumId, int offset, int limit
    ) throws ServerException;

    List<UserRatingView> getUsersRatings(
            int offset, int limit
    ) throws ServerException;

    List<UserRatingView> getUsersRatingsInForum(
            int forumId, int offset, int limit
    ) throws ServerException;
}
