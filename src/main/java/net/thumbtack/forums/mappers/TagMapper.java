package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.model.Tag;

import org.apache.ibatis.annotations.*;

public interface TagMapper {
    @Insert("INSERT INTO available_tags (tag_name) VALUES(#{tag.name})")
    @Options(useGeneratedKeys = true, keyProperty = "tag.id")
    Integer save(@Param("tag") Tag tag);

    @Insert(
            "INSERT INTO message_tags (tag_id, message_id) VALUES" +
                    "((SELECT id FROM available_tags WHERE tag_name = #{tag}), #{msg.id})"
    )
    void saveMessageForTag(@Param("tag") String tagName, @Param("msg") MessageItem message);

    @Select("SELECT id, tag_name FROM available_tags WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id", javaType = Integer.class),
            @Result(property = "name", column = "tag_name", javaType = String.class),
    })
    Tag findById(@Param("id") int id);

    @Select("SELECT id, tag_name FROM available_tags WHERE tag_name = #{name}")
    @Results({
            @Result(property = "id", column = "id", javaType = Integer.class),
            @Result(property = "name", column = "tag_name", javaType = String.class),
    })
    Tag findByName(@Param("name") String name);

    @Delete("DELETE FROM available_tags WHERE id = #{id}")
    void deleteById(@Param("id") int id);

    @Delete("DELETE FROM available_tags")
    void deleteAll();
}
