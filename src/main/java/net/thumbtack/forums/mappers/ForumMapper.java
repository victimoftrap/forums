package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.Forum;
import net.thumbtack.forums.model.enums.ForumType;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.time.LocalDateTime;
import java.util.List;

public interface ForumMapper {
    @Insert("INSERT INTO forums (forum_type, owner_id, name, readonly, created_at) " +
            "VALUES(" +
            "#{forum.type.name}, #{forum.owner.id}, " +
            "#{forum.name}, #{forum.readonly}, #{forum.createdAt}" +
            ")"
    )
    @Options(useGeneratedKeys = true, keyProperty = "forum.id")
    Integer save(@Param("forum") Forum forum);

    @Select("SELECT id, forum_type, owner_id, name, readonly, created_at FROM forums WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id", javaType = Integer.class),
            @Result(property = "type", column = "forum_type", javaType = ForumType.class),
            @Result(property = "owner", column = "owner_id", javaType = User.class,
                    one = @One(
                            select = "net.thumbtack.forums.mappers.UserMapper.getById",
                            fetchType = FetchType.LAZY
                    )
            ),
            @Result(property = "name", column = "name", javaType = String.class),
            @Result(property = "readonly", column = "readonly", javaType = Boolean.class),
            @Result(property = "createdAt", column = "created_at", javaType = LocalDateTime.class),
            @Result(property = "messageCount", column = "id", javaType = Integer.class,
                    one = @One(
                            select = "net.thumbtack.forums.mappers.MessageTreeMapper.getMessagesCount"
                    )
            ),
            @Result(property = "commentCount", column = "id", javaType = Integer.class,
                    one = @One(
                            select = "net.thumbtack.forums.mappers.MessageMapper.getCommentsCount"
                    )
            )
    })
    Forum getById(@Param("id") int id);

    @Select("SELECT id, forum_type, owner_id, name, readonly, created_at FROM forums")
    @Results({
            @Result(property = "id", column = "id", javaType = Integer.class),
            @Result(property = "type", column = "forum_type", javaType = ForumType.class),
            @Result(property = "owner", column = "owner_id", javaType = User.class,
                    one = @One(
                            select = "net.thumbtack.forums.mappers.UserMapper.getById",
                            fetchType = FetchType.LAZY
                    )
            ),
            @Result(property = "name", column = "name", javaType = String.class),
            @Result(property = "readonly", column = "readonly", javaType = Boolean.class),
            @Result(property = "createdAt", column = "created_at", javaType = LocalDateTime.class),
            @Result(property = "messageCount", column = "id", javaType = Integer.class,
                    one = @One(
                            select = "net.thumbtack.forums.mappers.MessageTreeMapper.getMessagesCount",
                            fetchType = FetchType.LAZY
                    )
            ),
            @Result(property = "commentCount", column = "id", javaType = Integer.class,
                    one = @One(
                            select = "net.thumbtack.forums.mappers.MessageMapper.getCommentsCount",
                            fetchType = FetchType.LAZY
                    )
            )
    })
    List<Forum> getAll();

    @Update("UPDATE forums SET readonly = COALESCE(#{forum.readonly}, readonly)")
    void update(@Param("forum") Forum forum);

    @Delete("DELETE FROM forums WHERE id = #{id}")
    void deleteById(@Param("id") int id);

    @Delete("DELETE FROM forums")
    void deleteAll();
}
