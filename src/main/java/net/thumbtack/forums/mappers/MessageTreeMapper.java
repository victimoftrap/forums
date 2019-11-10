package net.thumbtack.forums.mappers;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface MessageTreeMapper {
    @Select("SELECT COUNT(*) FROM messages_tree WHERE forum_id = #{id}")
    int countMessages(@Param("id") int forumId);
}
