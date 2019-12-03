package net.thumbtack.forums.dao;

import net.thumbtack.forums.model.MessageItem;

public interface MessageDao {
    MessageItem saveMessageItem(MessageItem item);

    void deleteById(int id);

    void publish(MessageItem item);
}
