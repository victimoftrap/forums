package net.thumbtack.forums.dao;

import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.Forum;

public interface RatingDao {
    void upsertRating(MessageItem message, User user, int rating) throws ServerException;

    void rate(MessageItem message, User user, int rating) throws ServerException;

    void changeRating(MessageItem message, User user, int rating) throws ServerException;

    void deleteRate(MessageItem message, User user) throws ServerException;

    double getMessageRating(MessageItem item) throws ServerException;
}
