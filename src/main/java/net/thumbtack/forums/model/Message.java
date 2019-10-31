package net.thumbtack.forums.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Message {
    private Integer id;
    private Forum forum;
    private User owner;
    private MessageStates state;
    private MessagePriorities priority;
    private String subject;
    private String body;
    private Integer rating;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<Tag> tags;
    private List<Comment> comments;

    public Message(Integer id, Forum forum, User owner, MessageStates state, MessagePriorities priority,
                   String subject, String body, Integer rating, Timestamp createdAt, Timestamp updatedAt,
                   List<Tag> tags, List<Comment> comments) {
        this.id = id;
        this.forum = forum;
        this.owner = owner;
        this.state = state;
        this.priority = priority;
        this.subject = subject;
        this.body = body;
        this.rating = rating;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.tags = tags;
        this.comments = comments;
    }

    public Message(Integer id, Forum forum, User owner, String state, String priority,
                   String subject, String body, Integer rating,
                   Timestamp createdAt, Timestamp updatedAt,
                   List<Tag> tags, List<Comment> comments) {
        this(id, forum, owner, MessageStates.valueOf(state), MessagePriorities.valueOf(priority),
                subject, body, rating, createdAt, updatedAt, tags, comments
        );
    }

    public Message(Forum forum, User owner, String state, String priority,
                   String subject, String body, Integer rating,
                   Timestamp createdAt, Timestamp updatedAt,
                   List<Tag> tags, List<Comment> comments) {
        this(0, forum, owner, state, priority,
                subject, body, rating, createdAt, updatedAt, tags, comments
        );
    }

    public Message(Integer id, Forum forum, User owner, String state, String priority,
                   String subject, String body, Integer rating,
                   Timestamp createdAt, Timestamp updatedAt) {
        this(id, forum, owner, state, priority,
                subject, body, rating, createdAt, updatedAt,
                new ArrayList<>(), new ArrayList<>()
        );
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Forum getForum() {
        return forum;
    }

    public void setForum(Forum forum) {
        this.forum = forum;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public MessageStates getState() {
        return state;
    }

    public void setState(MessageStates state) {
        this.state = state;
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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
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
        Message message = (Message) o;
        return Objects.equals(getId(), message.getId()) &&
                Objects.equals(getForum(), message.getForum()) &&
                Objects.equals(getOwner(), message.getOwner()) &&
                getState() == message.getState() &&
                getPriority() == message.getPriority() &&
                Objects.equals(getSubject(), message.getSubject()) &&
                Objects.equals(getBody(), message.getBody()) &&
                Objects.equals(getRating(), message.getRating()) &&
                Objects.equals(getCreatedAt(), message.getCreatedAt()) &&
                Objects.equals(getUpdatedAt(), message.getUpdatedAt()) &&
                Objects.equals(getTags(), message.getTags()) &&
                Objects.equals(getComments(), message.getComments());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getId(), getForum(), getOwner(), getState(), getPriority(),
                getSubject(), getBody(), getRating(), getCreatedAt(), getUpdatedAt(),
                getTags(), getComments()
        );
    }
}
