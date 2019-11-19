package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.UserSession;

import org.apache.ibatis.annotations.*;

public interface SessionMapper {
    @Insert("INSERT INTO users_sessions (user_id, session_token) VALUES(#{user.id}, #{token})")
    Integer save(UserSession session);

    @Select("SELECT user_id, session_token FROM users_sessions WHERE session_token = #{token}")
    @Results({
            @Result(property = "user", column = "user_id", javaType = User.class,
                    one = @One(select = "net.thumbtack.forums.mappers.UserMapper.getById")
            ),
            @Result(property = "token", column = "session_token", javaType = String.class)
    })
    UserSession getByToken(String token);

    @Select("SELECT session_token FROM users_sessions WHERE user_id = #{id}")
    String getSessionToken(User user);

    @Delete("DELETE FROM users_sessions WHERE session_token = #{token}")
    void deleteByToken(String token);

    @Delete("DELETE FROM users_sessions")
    void deleteAll();
}
