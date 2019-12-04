package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.UserSession;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.enums.UserRole;
import net.thumbtack.forums.daoimpl.provider.UserDaoProvider;

import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

public interface UserMapper {
    @Insert({"INSERT INTO users ",
            "(role, username, email, password, registered_at, deleted, banned_until, ban_count) ",
            "VALUES(",
            "#{role.name}, #{username}, ",
            "#{email}, #{password}, ",
            "#{registeredAt}, #{deleted}, ",
            "#{bannedUntil}, #{banCount}",
            ")"
    })
    @Options(useGeneratedKeys = true)
    Integer save(User user);

    @Select("SELECT id, role, LOWER(username) AS username, email, password, " +
            "registered_at, deleted, banned_until, ban_count " +
            "FROM users WHERE id = #{id}"
    )
    @Results(id = "userResult",
            value = {
                    @Result(property = "registeredAt", column = "registered_at", javaType = LocalDateTime.class),
                    @Result(property = "bannedUntil", column = "banned_until", javaType = LocalDateTime.class)
            }
    )
    User getById(int id);

    @Select({"<script>",
            "SELECT id, role, LOWER(username) AS username, email, password,",
            "registered_at, deleted, banned_until, ban_count",
            "FROM users",
            "WHERE id = #{id}",
            "<if test='deleted == false'>",
            " AND deleted = FALSE",
            "</if>",
            "</script>"
    })
    @ResultMap("userResult")
    User getByIdAndDeleted(@Param("id") int id, @Param("deleted") boolean deleted);

    @Select("SELECT id, role, LOWER(username) AS username, email, password, " +
            "registered_at, deleted, banned_until, ban_count " +
            "FROM users WHERE username = LOWER(#{name})"
    )
    @ResultMap("userResult")
    User getByName(@Param("name") String name);

    @Select({"<script>",
            "SELECT id, role, LOWER(username) AS username, email, password,",
            "registered_at, deleted, banned_until, ban_count",
            "FROM users",
            "WHERE username = LOWER(#{name})",
            "<if test='deleted == false'>",
            " AND deleted = FALSE",
            "</if>",
            "</script>"
    })
    @ResultMap("userResult")
    User getByNameAndDeleted(@Param("name") String name, @Param("deleted") boolean deleted);

    @Select("SELECT id, role, LOWER(username) AS username, email, password, " +
            "registered_at, deleted, banned_until, ban_count FROM users"
    )
    @ResultMap("userResult")
    List<User> getAll();

    @Select({"<script>",
            "SELECT id, role, LOWER(username) AS username, email, password,",
            "registered_at, deleted, banned_until, ban_count",
            "FROM users",
            "<if test='deleted == false'>",
            " WHERE deleted = FALSE",
            "</if>",
            "</script>"
    })
    @ResultMap("userResult")
    List<User> getAllAndDeleted(@Param("deleted") boolean deleted);

    @SelectProvider(method = "getAllUsersWithSessions", type = UserDaoProvider.class)
    @Results({
            @Result(property = "user.id", column = "id", javaType = int.class),
            @Result(property = "user.role", column = "role", javaType = UserRole.class),
            @Result(property = "user.username", column = "username", javaType = String.class),
            @Result(property = "user.email", column = "email", javaType = String.class),
            @Result(property = "user.password", column = "password", javaType = String.class),
            @Result(property = "user.registeredAt", column = "registered_at", javaType = LocalDateTime.class),
            @Result(property = "user.deleted", column = "deleted", javaType = boolean.class),
            @Result(property = "user.bannedUntil", column = "banned_until", javaType = LocalDateTime.class),
            @Result(property = "user.banCount", column = "ban_count", javaType = int.class),
            @Result(property = "token", column = "session_token", javaType = String.class)
    })
    List<UserSession> getAllWithSessions();

    @Update({"UPDATE users SET ",
            "role = COALESCE(#{role.name}, role), ",
            "email = COALESCE(#{email}, email), ",
            "password = COALESCE(#{password}, password), ",
            "banned_until = #{bannedUntil}, ",
            "ban_count = COALESCE(#{banCount}, ban_count) ",
            "WHERE id = #{id}"
    })
    void update(User user);

    @Update("UPDATE users SET deleted = TRUE WHERE id = #{id}")
    void deactivateById(int id);

    @Delete("DELETE FROM users")
    void deleteAll();
}
