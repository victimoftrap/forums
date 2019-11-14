package net.thumbtack.forums.dao;

import net.thumbtack.forums.model.User;

public interface BanDao {
    void banUser(User user);
}
