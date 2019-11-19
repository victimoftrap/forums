package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.User;

import net.thumbtack.forums.model.enums.UserRole;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

public interface UserMapper {
	@Insert("INSERT INTO users " +
            "(role, username, email, password, registered_at, deleted, banned_until, ban_count) " +
            "VALUES(" +
            "#{role.name}, #{username}, " +
            "#{email}, #{password}, " +
            "#{registeredAt}, #{deleted}, " +
            "#{bannedUntil}, #{banCount}" +
            ")"
    )
    @Options(useGeneratedKeys = true)
    Integer save(User user);

    @Select("SELECT id, role, username, email, password, registered_at, " +
            "deleted, banned_until, ban_count FROM users WHERE id = #{id}"
    )
    @Results({
            @Result(property = "registeredAt", column = "registered_at", javaType = LocalDateTime.class),
            @Result(property = "bannedUntil", column = "banned_until", javaType = LocalDateTime.class)
    })
    User getById(int id);

    @Select({"<script>",
            "SELECT id, role, username, email, password, registered_at, ",
            "deleted, banned_until, ban_count FROM users ",
            "WHERE id = #{id}",
            "<if test='deleted == false'>",
            " AND deleted = FALSE",
            "</if>",
            "</script>"
    })
    @Results({
            @Result(property = "registeredAt", column = "registered_at", javaType = LocalDateTime.class),
            @Result(property = "bannedUntil", column = "banned_until", javaType = LocalDateTime.class)
    })
    User getByIdAndDeleted(@Param("id") int id, @Param("deleted") boolean deleted);

    @Select("SELECT id, role, username, email, password, registered_at, " +
            "deleted, banned_until, ban_count FROM users WHERE username = #{name}"
    )
    @Results({
            @Result(property = "registeredAt", column = "registered_at", javaType = LocalDateTime.class),
            @Result(property = "bannedUntil", column = "banned_until", javaType = LocalDateTime.class)
    })
    User getByName(String name);

    @Select({"<script>",
            "SELECT id, role, username, email, password, registered_at, ",
            "deleted, banned_until, ban_count FROM users ",
            "WHERE username = #{name}",
            "<if test='deleted == false'>",
            " AND deleted = FALSE",
            "</if>",
            "</script>"
    })
    @Results({
            @Result(property = "registeredAt", column = "registered_at", javaType = LocalDateTime.class),
            @Result(property = "bannedUntil", column = "banned_until", javaType = LocalDateTime.class)
    })
    User getByNameAndDeleted(@Param("name") String name, @Param("deleted") boolean deleted);

    @Select("SELECT id, role, username, email, password, registered_at, " +
            "deleted, banned_until, ban_count FROM users"
    )
    @Results({
            @Result(property = "registeredAt", column = "registered_at", javaType = LocalDateTime.class),
            @Result(property = "bannedUntil", column = "banned_until", javaType = LocalDateTime.class)
    })
    List<User> getAll();

    @Select({"<script>",
            "SELECT id, role, username, email, password, registered_at, ",
            "deleted, banned_until, ban_count FROM users",
            "<if test='deleted == false'>",
            " WHERE deleted = FALSE",
            "</if>",
            "</script>"
    })
    @Results({
            @Result(property = "registeredAt", column = "registered_at", javaType = LocalDateTime.class),
            @Result(property = "bannedUntil", column = "banned_until", javaType = LocalDateTime.class)
    })
    List<User> getAllAndDeleted(@Param("deleted") boolean deleted);

    @Update("UPDATE users SET " +
            "role = COALESCE(#{role.name}, role), " +
            "email = COALESCE(#{email}, email), " +
            "password = COALESCE(#{password}, password), " +
            "banned_until = #{bannedUntil}, " +
            "ban_count = COALESCE(#{banCount}, ban_count) " +
            "WHERE id = #{id}"
    )
    void update(User user);

    @Update("UPDATE users SET deleted = TRUE WHERE id = #{id}")
    void deactivateById(int id);

    @Delete("DELETE FROM users")
    void deleteAll();
}
