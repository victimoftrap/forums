package net.thumbtack.forums.dao;

import net.thumbtack.forums.model.Forum;

import java.util.List;

public interface ForumDao {
    Forum save(Forum forum);

    Forum getById(int id);

    List<Forum> getAll();

    void update(Forum forum);

    void deleteById(int id);

    void deleteAll();
}
