package net.thumbtack.forums.dao;

import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.model.enums.MessageOrder;

import java.util.List;

public interface MessageDao {
    MessageItem saveMessageItem(MessageItem item) throws ServerException;

    MessageItem getMessageById(int id) throws ServerException;

    void publish(MessageItem item) throws ServerException;

    void deleteMessageById(int id) throws ServerException;

    void deleteAll() throws ServerException;
}
