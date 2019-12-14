package net.thumbtack.forums.dao;

import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.UserSession;

import java.util.List;

public interface UserDao {
    User save(User user) throws ServerException;

    UserSession save(User user, UserSession session) throws ServerException;

    User getById(int id) throws ServerException;

    User getById(int id, boolean deleted) throws ServerException;

    User getByName(String name) throws ServerException;

    User getByName(String name, boolean deleted) throws ServerException;

    List<User> getAll() throws ServerException;

    List<User> getAll(boolean withDeleted) throws ServerException;

    List<UserSession> getAllWithSessions() throws ServerException;

    void update(User user) throws ServerException;

    void deactivateById(int id) throws ServerException;

    void deleteAll() throws ServerException;
}
