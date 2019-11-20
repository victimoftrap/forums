package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.Tag;
import net.thumbtack.forums.model.MessageTree;

import org.apache.ibatis.annotations.*;

public interface TagMapper {
    @Insert("INSERT INTO available_tags (tag_name) VALUES(#{tag.name})")
    @Options(useGeneratedKeys = true)
    Integer save(Tag tag);

    @Insert({"INSERT INTO message_tags (tag_id, message_id) ",
            "VALUES(",
            "(SELECT id FROM available_tags WHERE tag_name = #{tag.name}), ",
            "#{msg.rootMessage.id}",
            ")"
    })
    void saveMessageForTag(@Param("tag") Tag tag, @Param("msg") MessageTree message);

    @Insert({"<script>",
            "INSERT INTO message_tags (tag_id, message_id) VALUES",
            "<foreach item='tag' collection='msg.tags' separator=','>",
            "(",
            "(SELECT id FROM available_tags WHERE tag_name = #{tag.name}), ",
            "#{msg.rootMessage.id}",
            ")",
            "</foreach>",
            " ON DUPLICATE KEY UPDATE tag_name = tag_name",
            "</script>"
    })
    void saveMessageForAllTags(@Param("msg") MessageTree message);

    @Select("SELECT id, tag_name FROM available_tags WHERE id = #{id}")
    Tag getById(int id);

    @Select("SELECT id, tag_name FROM available_tags WHERE tag_name = #{name}")
    Tag getByName(String name);

    @Delete("DELETE FROM available_tags WHERE id = #{id}")
    void deleteById(int id);

    @Delete("DELETE FROM available_tags")
    void deleteAll();
}
