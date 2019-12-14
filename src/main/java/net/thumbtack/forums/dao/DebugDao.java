package net.thumbtack.forums.dao;

import net.thumbtack.forums.exception.ServerException;

public interface DebugDao {
    void clear() throws ServerException;
}
