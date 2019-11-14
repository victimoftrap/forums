package net.thumbtack.forums.dao;

import net.thumbtack.forums.model.User;

import java.util.List;

public interface UserDao {
    User save(User user);

    User getById(int id);

    User getById(int id, boolean deleted);

    User getByName(String name);

    User getByName(String name, boolean deleted);

    List<User> getAll();

    List<User> getAll(boolean withDeleted);

    void update(User user);

    void deactivateById(int id);

    void deleteAll();
}
