package net.thumbtack.forums.dao;

import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.model.Forum;

import java.util.List;

public interface ForumDao {
    Forum save(Forum forum) throws ServerException;

    Forum getById(int id) throws ServerException;

    List<Forum> getAll() throws ServerException;

    void update(Forum forum) throws ServerException;

    void deleteById(int id) throws ServerException;

    void deleteAll() throws ServerException;
}
