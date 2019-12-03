package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.HistoryItem;
import net.thumbtack.forums.model.MessageItem;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

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
            "<foreach item='hist' collection='item.history', separator=','>",
            "(#{hist.messageId}, #{hist.body}, #{hist.state.name}, #{hist.createdAt})",
            "</foreach>",
            "</script>"
    })
    void saveAllHistory(MessageItem item);

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
}
