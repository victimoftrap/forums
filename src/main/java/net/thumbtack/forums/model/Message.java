package net.thumbtack.forums.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Message extends ForumMessage {
    private MessagePriorities priority;
    private String subject;
    private List<Tag> tags;
    private List<Comment> comments;

    public Message(int id, Forum forum, User owner, MessageStates state, MessagePriorities priority,
                   String subject, String body, int rating, Timestamp createdAt, Timestamp updatedAt,
                   List<Tag> tags, List<Comment> comments) {
        super(id, forum, owner, state, body, rating, createdAt, updatedAt);
        this.priority = priority;
        this.subject = subject;
        this.tags = tags;
        this.comments = comments;
    }

    public Message(int id, Forum forum, User owner, String state, String priority,
                   String subject, String body, int rating,
                   Timestamp createdAt, Timestamp updatedAt,
                   List<Tag> tags, List<Comment> comments) {
        this(id, forum, owner, MessageStates.valueOf(state), MessagePriorities.valueOf(priority),
                subject, body, rating, createdAt, updatedAt, tags, comments
        );
    }

    public Message(Forum forum, User owner, String state, String priority,
                   String subject, String body, int rating,
                   Timestamp createdAt, Timestamp updatedAt,
                   List<Tag> tags, List<Comment> comments) {
        this(0, forum, owner, state, priority,
                subject, body, rating, createdAt, updatedAt, tags, comments
        );
    }

    public Message(int id, Forum forum, User owner, String state, String priority,
                   String subject, String body, int rating,
                   Timestamp createdAt, Timestamp updatedAt) {
        this(id, forum, owner, state, priority,
                subject, body, rating, createdAt, updatedAt,
                new ArrayList<>(), new ArrayList<>()
        );
    }

    public Message(int id, Forum forum, User owner, MessageStates state, MessagePriorities priority,
                   String subject, List<String> bodies, int rating,
                   Timestamp createdAt, Timestamp updatedAt, List<Tag> tags, List<Comment> comments) {
        super(id, forum, owner, state, bodies, rating, createdAt, updatedAt);
        this.priority = priority;
        this.subject = subject;
        this.tags = tags;
        this.comments = comments;
    }

    public MessagePriorities getPriority() {
        return priority;
    }

    public void setPriority(MessagePriorities priority) {
        this.priority = priority;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        if (!super.equals(o)) return false;
        Message message = (Message) o;
        return getPriority() == message.getPriority() &&
                Objects.equals(getSubject(), message.getSubject()) &&
                Objects.equals(getTags(), message.getTags()) &&
                Objects.equals(getComments(), message.getComments());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getPriority(), getSubject(), getTags(), getComments());
    }
}
