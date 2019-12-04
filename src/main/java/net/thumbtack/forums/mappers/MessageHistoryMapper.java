package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.HistoryItem;
import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.model.enums.MessageState;

import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageHistoryMapper {
    @Insert({"INSERT INTO message_history",
            "(message_id, body, state, created_at)",
            "VALUES (#{messageId}, #{body}, #{state.name}, #{createdAt})"
    })
    void saveHistory(HistoryItem history);

    @Insert({"<script>",
            "INSERT INTO message_history",
            "(message_id, body, state, created_at)",
            "VALUES",
            "<foreach item='hist' collection='item.history' separator=','>",
            "(#{hist.messageId}, #{hist.body}, #{hist.state.name}, #{hist.createdAt})",
            "</foreach>",
            "</script>"
    })
    void saveAllHistory(MessageItem item);

    @Select({"SELECT message_id, body, state, created_at",
            "FROM message_history WHERE message_id = #{id}",
            "ORDER BY created_at DESC"
    })
    @Results(id = "historyResult",
            value = {
                    @Result(property = "messageId", column = "message_id", javaType = int.class),
                    @Result(property = "body", column = "body", javaType = String.class),
                    @Result(property = "state", column = "state", javaType = MessageState.class),
                    @Result(property = "createdAt", column = "created_at", javaType = LocalDateTime.class)
            }
    )
    List<HistoryItem> getMessageHistory(@Param("id") int messageId);

    @Select({"SELECT message_id, body, state, created_at",
            "FROM message_history",
            "WHERE message_id = #{id} AND state = #{state.name}",
            "ORDER BY created_at DESC LIMIT 1"
    })
    @ResultMap("historyResult")
    HistoryItem getLatestMessageHistory(@Param("id") int messageId, @Param("state") MessageState state);

    @Update({"UPDATE message_history",
            "SET body = #{body}",
            "WHERE message_id = #{messageId} AND state = 'UNPUBLISHED'"
    })
    void editUnpublishedHistory(HistoryItem history);

    @Update({"UPDATE message_history",
            "SET state = #{state.name},",
            "SET created_at = #{createdAt}",
            "WHERE message_id = #{messageId} AND state = 'UNPUBLISHED'"
    })
    void publishMessage(HistoryItem history);

    @Delete("DELETE FROM message_history WHERE message_id = #{id} AND state = 'UNPUBLISHED'")
    void deleteRejectedHistory(int id);

    @Delete("DELETE FROM message_history")
    void deleteAll();
}
