package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.Forum;
import net.thumbtack.forums.model.ForumTypes;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.sql.Timestamp;

public interface ForumMapper {
    @Insert(
            "INSERT INTO forums (forum_type, owner_id, name, readonly, created_at) " +
                    "VALUES(#{forum.type.name}, #{forum.owner.id}, " +
                    "#{forum.name}, #{forum.readonly}, #{forum.createdAt}" +
                    ")"
    )
    @Options(useGeneratedKeys = true, keyProperty = "forum.id")
    Integer save(@Param("forum") Forum forum);

    @Select(
            "SELECT id, forum_type, owner_id, name, readonly, created_at FROM forums WHERE id = #{id}"
    )
    @Results({
            @Result(property = "id", column = "id", javaType = Integer.class),
            @Result(property = "type", column = "forum_type", javaType = ForumTypes.class),
            @Result(property = "owner", column = "owner_id", javaType = Forum.class,
                    one = @One(
                            select = "net.thumbtack.forums.mappers.UserMapper.findById",
                            fetchType = FetchType.LAZY
                    )
            ),
            @Result(property = "name", column = "name", javaType = String.class),
            @Result(property = "readonly", column = "readonly", javaType = Boolean.class),
            @Result(property = "createdAt", column = "created_at", javaType = Timestamp.class)
    })
    Forum findById(@Param("id") Integer id);

    @Update(
            "UPDATE forums SET forum_type = #{forum.type.name}, name = #{forum.name}, readonly = #{forum.readonly}"
    )
    void update(@Param("forum") Forum forum);

    @Delete("DELETE FROM forums WHERE id = #{id}")
    void deleteById(@Param("id") Integer id);

    @Delete("DELETE FROM forums")
    void deleteAll();
}
