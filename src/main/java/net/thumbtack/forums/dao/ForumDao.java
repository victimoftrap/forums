package net.thumbtack.forums.dao;

import net.thumbtack.forums.model.Forum;

public interface ForumDao {
    Forum save(Forum forum);

    Forum findById(Integer id);

    void update(Forum forum);

    void deleteById(Integer id);
}
