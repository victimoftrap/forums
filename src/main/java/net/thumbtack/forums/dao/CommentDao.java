package net.thumbtack.forums.dao;

import net.thumbtack.forums.model.MessageItem;

public interface CommentDao {
    MessageItem saveComment(MessageItem item);
}
