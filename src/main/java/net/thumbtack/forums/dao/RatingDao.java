package net.thumbtack.forums.dao;

import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.Forum;

public interface RatingDao {
    void upsertRating(MessageItem message, User user, int rating);

    void rate(MessageItem message, User user, int rating);

    void changeRating(MessageItem message, User user, int rating);

    void deleteRate(MessageItem message, User user);

    double getMessageRating(MessageItem item);
}
