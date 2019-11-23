package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.UserSession;

import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

public interface SessionMapper {
    @Insert("INSERT INTO users_sessions (user_id, session_token) " +
            "VALUES(#{user.id}, #{token}) " +
            "ON DUPLICATE KEY UPDATE session_token = #{token}"
    )
    Integer upsertSession(UserSession session);

    @Select("SELECT user_id, session_token FROM users_sessions WHERE session_token = #{token}")
    @Results({
            @Result(property = "user", column = "user_id", javaType = User.class,
                    one = @One(select = "net.thumbtack.forums.mappers.UserMapper.getById")
            ),
            @Result(property = "token", column = "session_token", javaType = String.class)
    })
    UserSession getSessionByToken(String token);

    @Select({"SELECT id, role, username, email, password, registered_at, deleted, banned_until, ban_count ",
            "FROM users WHERE id = (",
            "SELECT user_id FROM users_sessions WHERE session_token = #{token}",
            ")"
    })
    @Results({
            @Result(property = "registeredAt", column = "registered_at", javaType = LocalDateTime.class),
            @Result(property = "bannedUntil", column = "banned_until", javaType = LocalDateTime.class)
    })
    User getUserByToken(String token);

    @Delete("DELETE FROM users_sessions WHERE session_token = #{token}")
    void deleteByToken(String token);

    @Delete("DELETE FROM users_sessions")
    void deleteAll();
}
