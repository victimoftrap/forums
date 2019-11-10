package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.enums.UserRole;

import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

public interface UserMapper {
    @Insert(
            "INSERT INTO users " +
                    "(role, username, email, password, registered_at, deleted, banned_until, ban_count, permanent) " +
                    "VALUES(#{user.role.name}, #{user.userName}, #{user.email}, #{user.password}, " +
                    "#{user.registeredAt}, #{user.areDeleted} #{user.bannedUntil}, " +
                    "#{user.banCount}, #{user.arePermanent}" +
                    ")"
    )
    @Options(useGeneratedKeys = true, keyProperty = "user.id")
    Integer save(@Param("user") User user);

    @Select(
            "SELECT id, role, username, email, password, registered_at, deleted, " +
                    "banned_until, ban_count, permanent FROM users WHERE id = #{id}"
    )
    @Results({
            @Result(property = "id", column = "id", javaType = Integer.class),
            @Result(property = "role", column = "role", javaType = UserRole.class),
            @Result(property = "userName", column = "username", javaType = String.class),
            @Result(property = "email", column = "email", javaType = String.class),
            @Result(property = "password", column = "password", javaType = String.class),
            @Result(property = "areDeleted", column = "password", javaType = String.class),
            @Result(property = "registeredAt", column = "registered_at", javaType = LocalDateTime.class),
            @Result(property = "bannedUntil", column = "banned_until", javaType = LocalDateTime.class),
            @Result(property = "banCount", column = "ban_count", javaType = Integer.class),
            @Result(property = "arePermanent", column = "permanent", javaType = Boolean.class)
    })
    User findById(@Param("id") int id);

    @Select(
            "SELECT id, role, username, email, password, registered_at, deleted, " +
                    "banned_until, ban_count, permanent FROM users"
    )
    @Results({
            @Result(property = "id", column = "id", javaType = Integer.class),
            @Result(property = "role", column = "role", javaType = UserRole.class),
            @Result(property = "userName", column = "username", javaType = String.class),
            @Result(property = "email", column = "email", javaType = String.class),
            @Result(property = "password", column = "password", javaType = String.class),
            @Result(property = "areDeleted", column = "password", javaType = String.class),
            @Result(property = "registeredAt", column = "registered_at", javaType = LocalDateTime.class),
            @Result(property = "bannedUntil", column = "banned_until", javaType = LocalDateTime.class),
            @Result(property = "banCount", column = "ban_count", javaType = Integer.class),
            @Result(property = "arePermanent", column = "permanent", javaType = Boolean.class)
    })
    List<User> findAll();

    @Update(
            "UPDATE users SET " +
                    "role = #{user.role.name}, email = #{user.email}, password = #{user.password}, " +
                    "deleted = #{user.areDeleted}, banned_until = #{user.bannedUntil}, " +
                    "ban_count = #{user.banCount}, WHERE id = #{user.id}"
    )
    void update(@Param("user") User user);

    @Delete("DELETE FROM users WHERE id = #{id}")
    void deleteById(@Param("id") int id);

    @Delete("DELETE FROM users")
    void deleteAll();
}
