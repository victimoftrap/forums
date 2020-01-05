package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.Tag;
import net.thumbtack.forums.model.MessageTree;

import org.apache.ibatis.annotations.*;

import java.util.List;

public interface TagMapper {
    @Insert({"INSERT INTO available_tags (tag_name) VALUES( LOWER(#{name}) )",
            "ON DUPLICATE KEY UPDATE tag_name = tag_name"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Integer saveTag(Tag tag);

    @Insert({"<script>",
            "INSERT INTO available_tags (tag_name) VALUES",
            "<foreach item='tag' collection='list' separator=','>",
            "( LOWER(#{tag.name}) )",
            "</foreach>",
            "ON DUPLICATE KEY UPDATE tag_name = tag_name",
            "</script>"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Integer saveAllTags(List<Tag> tags);

    @Insert({"<script>",
            "INSERT INTO message_tags (tag_id, tree_id) VALUES",
            "<foreach item='tag' collection='tree.tags' separator=','>",
            "(#{tag.id}, #{tree.id})",
            "</foreach>",
            "</script>"
    })
    void bindMessageAndTags(@Param("tree") MessageTree message);

    @Insert({"<script>",
            "INSERT INTO message_tags (tag_id, tree_id) VALUES",
            "<foreach item='tag' collection='tree.tags' separator=','>",
            "(",
            "(SELECT id FROM available_tags WHERE tag_name = LOWER(#{tag.name}) ),",
            "#{tree.id}",
            ")",
            "</foreach>",
            "</script>"
    })
    void safeBindMessageAndTags(@Param("tree") MessageTree message);

    @Select("SELECT id, tag_name FROM available_tags WHERE id = #{id}")
    @Results(id = "tagResult",
            value = {
                    @Result(property = "id", column = "id", javaType = int.class),
                    @Result(property = "name", column = "tag_name", javaType = String.class)
            }
    )
    Tag getById(int id);

    @Select("SELECT id, tag_name FROM available_tags WHERE tag_name = LOWER(#{name})")
    @ResultMap("tagResult")
    Tag getByName(String name);

    @Select({"SELECT id, tag_name FROM available_tags WHERE id IN (",
            "SELECT tag_id FROM message_tags WHERE tree_id = #{treeId}",
            ")"
    })
    @ResultMap("tagResult")
    List<Tag> getMessageTreeTags(int treeId);

    @Delete("DELETE FROM available_tags WHERE id = #{id}")
    void deleteById(int id);

    @Delete("DELETE FROM available_tags")
    void deleteAll();
}
