package net.thumbtack.forums.daoimpl.provider;

public class MessageTreeDaoProvider {
    public String getMessageTreeByMessage() {
        return "WITH RECURSIVE msg_cte AS (\n" +
                "  SELECT id, owner_id, parent_message FROM messages WHERE id = #{id}\n" +
                "  UNION ALL\n" +
                "  SELECT m.id, m.owner_id, m.parent_message FROM messages m\n" +
                "  INNER JOIN msg_cte mc ON m.id = mc.parent_message\n" +
                ")\n" +
                "SELECT id, forum_id, root_message, subject, priority FROM messages_tree WHERE root_message = (\n" +
                "  SELECT id FROM msg_cte mc WHERE parent_message IS NULL\n" +
                ")";
    }
}
