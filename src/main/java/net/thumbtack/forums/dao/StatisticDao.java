package net.thumbtack.forums.dao;

import net.thumbtack.forums.view.MessageRatingView;
import net.thumbtack.forums.view.MessagesCountView;
import net.thumbtack.forums.view.UserRatingView;
import net.thumbtack.forums.exception.ServerException;

import java.util.List;

public interface StatisticDao {
    int getMessagesCount() throws ServerException;

    int getMessagesCount(int forumId) throws ServerException;

    int getCommentsCount() throws ServerException;

    int getCommentsCount(int forumId) throws ServerException;

    MessagesCountView getMessagesAndCommentsCount() throws ServerException;

    MessagesCountView getMessagesAndCommentsCount(int forumId) throws ServerException;

    List<MessageRatingView> getMessagesRatings(int offset, int limit) throws ServerException;

    List<MessageRatingView> getMessagesRatingsInForum(
            int forumId, int offset, int limit
    ) throws ServerException;

    List<UserRatingView> getUsersRatings(int offset, int limit) throws ServerException;

    List<UserRatingView> getUsersRatingsInForum(
            int forumId, int offset, int limit
    ) throws ServerException;
}
