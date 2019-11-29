package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.HistoryItem;

import net.thumbtack.forums.model.enums.MessageState;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageHistoryMapper {
    @Insert("INSERT INTO message_history " +
            "(message_id, body, state, created_at) " +
            "VALUES(#{messageId}, #{body}, #{state.name}, #{createdAt})"
    )
    @Options(useGeneratedKeys = true)
    Integer save(HistoryItem history);

    @Select({"<script>",
            "SELECT message_id, body, state, created_at FROM message_history",
            "WHERE message_id = #{id}",
            "<if test='unpublished == false'>",
            " AND state = 'PUBLISHED' ",
            "</if>",
            "ORDER BY created_at DESC",
            "<if test='allVersions == false'>",
            " LIMIT 1",
            "</if>",
            "</script>"
    })
    @Results({
            @Result(property = "state", column = "state", javaType = MessageState.class),
            @Result(property = "createdAt", column = "created_at", javaType = LocalDateTime.class)
    })
    List<HistoryItem> getHistory(@Param("id") int messageId,
                                 @Param("allVersions") boolean allVersions,
                                 @Param("unpublished") boolean unpublished
    );

    @Select("SELECT message_id, body, state, created_at " +
            "FROM message_history " +
            "WHERE message_id = #{id} " +
            "ORDER BY created_at DESC"
    )
    List<HistoryItem> getByMessageId(@Param("id") int messageId);

    @Update("UPDATE message_history SET " +
            "body = COALESCE(#{body}, body), " +
            "state = COALESCE(#{state.name}, state), " +
            "created_at = COALESCE(#{createdAt}, created_at) " +
            "WHERE message_id = #{messageId} AND state = 'UNPUBLISHED'"
    )
    void update(HistoryItem historyItem); // publish, reject, editUnpublished

    @Delete("DELETE FROM message_history WHERE message_id = #{hist.messageId} AND state = 'UNPUBLISHED'")
    void delete(@Param("hist") HistoryItem historyItem);

    @Delete("DELETE FROM message_history")
    void deleteAll();
}
