package net.thumbtack.forums.dao;

import net.thumbtack.forums.model.Message;

public interface MessageDao {
    Message save(Message message);

    Message findById(Integer id);

    void update(Message message);

    void deleteById(Integer id);

    void deleteAll();
}
