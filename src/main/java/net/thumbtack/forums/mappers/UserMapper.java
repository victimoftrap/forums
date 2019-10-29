package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.User;

import org.apache.ibatis.annotations.*;

import java.sql.Timestamp;

public interface UserMapper {
    @Insert(
            "INSERT INTO users (role, username, email, password, registered_at, banned_until, ban_count, permanent) " +
                    "VALUES(#{user.role.name}, #{user.userName}, #{user.email}, #{user.password}, " +
                    "#{user.registeredAt}, #{user.bannedUntil}, #{user.banCount}, #{user.arePermanent}" +
                    ")"
    )
    @Options(useGeneratedKeys = true, keyProperty = "user.id")
    User save(@Param("user") User user);

    @Select(
            "SELECT id, role, username, email, password, registered_at, " +
                    "banned_until, ban_count, permanent FROM users WHERE id = #{id}"
    )
    @Results({
            @Result(property = "id", column = "id", javaType = Integer.class),
            @Result(property = "userName", column = "username", javaType = String.class),
            @Result(property = "email", column = "email", javaType = String.class),
            @Result(property = "password", column = "password", javaType = String.class),
            @Result(property = "registeredAt", column = "registered_at", javaType = Timestamp.class),
            @Result(property = "bannedUntil", column = "banned_until", javaType = Timestamp.class),
            @Result(property = "banCount", column = "ban_count", javaType = Integer.class),
            @Result(property = "arePermanent", column = "permanent", javaType = Boolean.class)
    })
    User findById(@Param("id") Integer id);

    @Update(
            "UPDATE users SET " +
                    "role = #{upd.role.name}, email = #{upd.email}, password = #{upd.password}, " +
                    "banned_until = #{user.bannedUntil}, ban_count = #{user.banCount}" +
                    "permanent = #{user.arePermanent} WHERE id = #{upd.id}"
    )
    void update(@Param("upd") User user);

    @Delete(
            "DELETE FROM users WHERE id = #{id}"
    )
    void deleteById(@Param("id") Integer id);
}
