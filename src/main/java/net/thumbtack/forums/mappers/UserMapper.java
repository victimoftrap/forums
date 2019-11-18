package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.User;

import net.thumbtack.forums.model.enums.UserRole;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

public interface UserMapper {
// REVU user - лишнее
//	#{user.email} -------> #{email}
// параметр всего один, и так ясно	
// здесь и везде
	@Insert("INSERT INTO users " +
            "(role, username, email, password, registered_at, deleted, banned_until, ban_count) " +
            "VALUES(" +
            "#{user.role.name}, #{user.username}, " +
            "#{user.email}, #{user.password}, " +
            "#{user.registeredAt}, #{user.deleted}, " +
            "#{user.bannedUntil}, #{user.banCount}" +
            ")"
    )
    @Options(useGeneratedKeys = true, keyProperty = "user.id")
    Integer save(@Param("user") User user);

    @Select("SELECT id, role, username, email, password, registered_at, " +
            "deleted, banned_until, ban_count FROM users WHERE id = #{id}"
    )
    @Results({
        // REVU старайтесь давать полям класса и таблицы одинаковые имена
        // тогда соответствующий @Result можно будет не писать
    	// role тут не нужен, остальные можно сделать, чтобы было не нужно
            @Result(property = "role", column = "role", javaType = UserRole.class),
            @Result(property = "registeredAt", column = "registered_at", javaType = LocalDateTime.class),
            @Result(property = "deleted", column = "deleted", javaType = Boolean.class),
            @Result(property = "bannedUntil", column = "banned_until", javaType = LocalDateTime.class)
    })
    User getById(@Param("id") int id);

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
            @Result(property = "role", column = "role", javaType = UserRole.class),
            @Result(property = "registeredAt", column = "registered_at", javaType = LocalDateTime.class),
            @Result(property = "deleted", column = "deleted", javaType = Boolean.class),
            @Result(property = "bannedUntil", column = "banned_until", javaType = LocalDateTime.class)
    })
    User getByIdAndDeleted(@Param("id") int id, @Param("deleted") boolean deleted);

    @Select("SELECT id, role, username, email, password, registered_at, " +
            "deleted, banned_until, ban_count FROM users WHERE username = #{name}"
    )
    @Results({
            @Result(property = "role", column = "role", javaType = UserRole.class),
            @Result(property = "registeredAt", column = "registered_at", javaType = LocalDateTime.class),
            @Result(property = "deleted", column = "deleted", javaType = Boolean.class),
            @Result(property = "bannedUntil", column = "banned_until", javaType = LocalDateTime.class)
    })
    User getByName(@Param("name") String name);

    @Select({"<script>",
            "SELECT id, role, username, email, password, registered_at, ",
            "deleted, banned_until, ban_count FROM users ",
            "WHERE username = #{name}",
            "<if test='deleted == false'>",
            " AND deleted = #{deleted}",
            "</if>",
            "</script>"
    })
    @Results({
            @Result(property = "role", column = "role", javaType = UserRole.class),
            @Result(property = "registeredAt", column = "registered_at", javaType = LocalDateTime.class),
            @Result(property = "deleted", column = "deleted", javaType = Boolean.class),
            @Result(property = "bannedUntil", column = "banned_until", javaType = LocalDateTime.class)
    })
    User getByNameAndDeleted(@Param("name") String name, @Param("deleted") boolean deleted);

    @Select("SELECT id, role, username, email, password, registered_at, " +
            "deleted, banned_until, ban_count FROM users"
    )
    @Results({
            @Result(property = "role", column = "role", javaType = UserRole.class),
            @Result(property = "registeredAt", column = "registered_at", javaType = LocalDateTime.class),
            @Result(property = "deleted", column = "deleted", javaType = Boolean.class),
            @Result(property = "bannedUntil", column = "banned_until", javaType = LocalDateTime.class)
    })
    List<User> getAll();

    @Select({"<script>",
            "SELECT id, role, username, email, password, registered_at, ",
            "deleted, banned_until, ban_count FROM users ",
            "<if test='deleted == false'>",
            " WHERE deleted = FALSE",
            "</if>",
            "</script>"
    })
    @Results({
            @Result(property = "role", column = "role", javaType = UserRole.class),
            @Result(property = "registeredAt", column = "registered_at", javaType = LocalDateTime.class),
            @Result(property = "deleted", column = "deleted", javaType = Boolean.class),
            @Result(property = "bannedUntil", column = "banned_until", javaType = LocalDateTime.class)
    })
    List<User> getAllAndDeleted(@Param("deleted") boolean deleted);

    @Update("UPDATE users SET " +
            "role = COALESCE(#{user.role.name}, role), " +
            "email = COALESCE(#{user.email}, email), " +
            "password = COALESCE(#{user.password}, password), " +
            "banned_until = #{user.bannedUntil}, " +
            "ban_count = COALESCE(#{user.banCount}, ban_count) " +
            "WHERE id = #{user.id}"
    )
    void update(@Param("user") User user);

    @Update("UPDATE users SET deleted = TRUE WHERE id = #{id}")
    void deactivateById(@Param("id") int id);

    @Delete("DELETE FROM users")
    void deleteAll();
}
