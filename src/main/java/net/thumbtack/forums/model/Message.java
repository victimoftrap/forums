package net.thumbtack.forums.model;

import java.sql.Timestamp;
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

    public Message(Integer id, Forum forum, User owner,
                   MessageStates state, MessagePriorities priority,
                   String subject, String body, Integer rating,
                   Timestamp createdAt, Timestamp updatedAt) {
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
    }

    public Message(Forum forum, User owner,
                   MessageStates state, MessagePriorities priority,
                   String subject, String body, Integer rating,
                   Timestamp createdAt, Timestamp updatedAt) {
        this(0, forum, owner, state, priority, subject, body, rating, createdAt, updatedAt);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message message = (Message) o;
        return Objects.equals(getId(), message.getId()) &&
                Objects.equals(getForum(), message.getForum()) &&
                Objects.equals(getOwner(), message.getOwner()) &&
                Objects.equals(getState(), message.getState()) &&
                Objects.equals(getPriority(), message.getPriority()) &&
                Objects.equals(getSubject(), message.getSubject()) &&
                Objects.equals(getBody(), message.getBody()) &&
                Objects.equals(getRating(), message.getRating()) &&
                Objects.equals(getCreatedAt(), message.getCreatedAt()) &&
                Objects.equals(getUpdatedAt(), message.getUpdatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getForum(), getOwner(),
                getState(), getPriority(),
                getSubject(), getBody(), getRating(),
                getCreatedAt(), getUpdatedAt()
        );
    }
}
