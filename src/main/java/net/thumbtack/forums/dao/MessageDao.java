package net.thumbtack.forums.dao;

import net.thumbtack.forums.model.MessageItem;

public interface MessageDao {
    MessageItem saveMessageItem(MessageItem item);

    MessageItem getMessageById(int id);

    void publish(MessageItem item);

    void deleteMessageById(int id);

    void deleteAll();
}
