package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.HistoryItem;

import net.thumbtack.forums.model.enums.MessageState;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageHistoryMapper {
    @Insert("INSERT INTO message_history (message_id, body, state, created_at) " +
            "VALUES(#{hist.messageId}, #{hist.body}, #{hist.state.name}, #{hist.createdAt})"
    )
    @Options(useGeneratedKeys = true, keyProperty = "hist.id")
    Integer save(@Param("hist") HistoryItem history);

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
    List<HistoryItem> getHistories(@Param("id") int messageId,
                                   @Param("allVersions") boolean allVersions,
                                   @Param("unpublished") boolean unpublished
    );

    @Update("UPDATE message_history SET " +
            "body = COALESCE(#{hist.body}, body), " +
            "state = COALESCE(#{hist.state.name}, state), " +
            "created_at = COALESCE(#{hist.createdAt}, created_at) " +
            "WHERE message_id = #{hist.messageId} AND state = 'UNPUBLISHED'"
    )
    void update(@Param("hist") HistoryItem historyItem); // publish, reject, editUnpublished

    @Delete("DELETE FROM message_history WHERE message_id = #{hist.messageId} AND state = 'UNPUBLISHED'")
    void delete(@Param("hist") HistoryItem historyItem);

    @Delete("DELETE FROM message_history")
    void deleteAll();
}
