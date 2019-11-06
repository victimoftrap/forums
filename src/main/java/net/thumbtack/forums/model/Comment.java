package net.thumbtack.forums.model;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

public class Comment extends ForumMessage {
    private Message referredMessage;

    public Comment(int id, Forum forum, User owner, Message referredMessage,
                   MessageStates state, String body, int rating,
                   Timestamp createdAt, Timestamp updatedAt) {
        super(id, forum, owner, state, body, rating, createdAt, updatedAt);
        this.referredMessage = referredMessage;
    }

    public Comment(int id, Forum forum, User owner, Message referredMessage,
                   String state, String body, int rating,
                   Timestamp createdAt, Timestamp updatedAt) {
        this(id, forum, owner, referredMessage, MessageStates.valueOf(state),
                body, rating, createdAt, updatedAt
        );
    }

    public Comment(Forum forum, User owner, Message referredMessage,
                   String state, String body, int rating,
                   Timestamp createdAt, Timestamp updatedAt) {
        this(0, forum, owner, referredMessage, state, body, rating, createdAt, updatedAt);
    }

    public Comment(int id, Forum forum, User owner, Message referredMessage,
                   MessageStates state, List<String> bodies, int rating,
                   Timestamp createdAt, Timestamp updatedAt) {
        super(id, forum, owner, state, bodies, rating, createdAt, updatedAt);
        this.referredMessage = referredMessage;
    }

    public Message getReferredMessage() {
        return referredMessage;
    }

    public void setReferredMessage(Message referredMessage) {
        this.referredMessage = referredMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment)) return false;
        if (!super.equals(o)) return false;
        Comment comment = (Comment) o;
        return Objects.equals(getReferredMessage(), comment.getReferredMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getReferredMessage());
    }
}
