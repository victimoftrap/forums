package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.HistoryItem;
import net.thumbtack.forums.model.enums.MessageState;

import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageHistoryMapper {
    @Insert({"INSERT INTO message_history",
            "(message_id, body, state, created_at)",
            "VALUES (#{id}, #{hist.body}, #{hist.state.name}, #{hist.createdAt})"
    })
    void saveHistory(@Param("id") int messageId, @Param("hist") HistoryItem history);

    @Select({"SELECT body, state, created_at",
            "FROM message_history WHERE message_id = #{id}",
            "ORDER BY created_at DESC"
    })
    @Results(id = "historyResult",
            value = {
                    @Result(property = "body", column = "body", javaType = String.class),
                    @Result(property = "state", column = "state", javaType = MessageState.class),
                    @Result(property = "createdAt", column = "created_at", javaType = LocalDateTime.class)
            }
    )
    List<HistoryItem> getMessageHistory(@Param("id") int messageId);

    @Select({"<script>",
            "SELECT body, state, created_at FROM message_history",
            "WHERE message_id = #{id}",
            "<if test='unpublished == false'>",
            "AND state == 'PUBLISHED'",
            "</if>",
            "ORDER BY created_at DESC",
            "<if test='allVersions == false'>",
            "LIMIT 1",
            "</if>",
            "</script>"
    })
    @ResultMap("historyResult")
    List<HistoryItem> getMessageHistoryByOptions(@Param("id") int messageId,
                                                 @Param("allVersions") boolean allVersions,
                                                 @Param("unpublished") boolean unpublished);

    @Select({"SELECT body, state, created_at",
            "FROM message_history",
            "WHERE message_id = #{id} AND state = #{state.name}",
            "ORDER BY created_at DESC LIMIT 1"
    })
    @ResultMap("historyResult")
    HistoryItem getLatestMessageHistory(@Param("id") int messageId, @Param("state") MessageState state);

    @Update({"UPDATE message_history",
            "SET body = #{hist.body}",
            "WHERE message_id = #{id} AND state = 'UNPUBLISHED'"
    })
    void editUnpublishedHistory(@Param("id") int messageId, @Param("hist") HistoryItem history);

    @Update({"UPDATE message_history",
            "SET state = COALESCE(#{hist.state.name}, state),",
            "created_at = COALESCE(#{hist.createdAt}, created_at)",
            "WHERE message_id = #{id} AND state = 'UNPUBLISHED'"
    })
    void updateMessageHistory(@Param("id") int messageId, @Param("hist") HistoryItem history);

    @Delete("DELETE FROM message_history WHERE message_id = #{id} AND state = 'UNPUBLISHED'")
    void deleteRejectedHistory(int id);

    @Delete("DELETE FROM message_history")
    void deleteAll();
}
