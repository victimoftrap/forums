package net.thumbtack.forums.dao;

import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.Forum;

public interface RatingDao {
    void rate(MessageItem message, User user, int rating);

    void changeRating(MessageItem message, User user, int rating);

    void deleteRate(MessageItem message, User user);

    int getMessageRating(MessageItem messageItem);

    int getUserRating(User user);

    int getUserRatingInForum(User user, Forum forum);
}
