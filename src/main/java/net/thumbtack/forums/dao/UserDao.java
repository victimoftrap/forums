package net.thumbtack.forums.dao;

import net.thumbtack.forums.model.User;

public interface UserDao {
    User save(User user);

    User findById(Integer id);

    void update(User user);

    void deleteById(Integer id);

    void deleteAll();
}
