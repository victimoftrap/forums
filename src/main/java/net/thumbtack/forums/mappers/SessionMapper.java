package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.UserSession;

import org.apache.ibatis.annotations.*;

public interface SessionMapper {
    @Insert(
            "INSERT INTO users_sessions (user_id, session_token) VALUES(#{session.userId}, #{session.token})"
    )
    Integer save(@Param("session") UserSession session);

    @Select("SELECT user_id, session_token FROM users_sessions WHERE token = #{token}")
    @Results({
            @Result(property = "userId", column = "user_id", javaType = Integer.class),
            @Result(property = "token", column = "session_token", javaType = String.class)
    })
    UserSession findByToken(@Param("token") String token);

    @Delete("DELETE FROM users_sessions WHERE session_token = #{token}")
    void deleteByToken(@Param("token") String token);
}
