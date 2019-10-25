package net.thumbtack.forums.model;

import java.sql.Timestamp;
import java.util.Objects;

public class Comment {
    private Integer id;
    private Integer referredToMessage;
    private User owner;
    private String state;
    private String priority;
    private String body;
    private Integer rating;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Comment(Integer id, Integer referredToMessage, User owner, String state, String priority,
                   String body, Integer rating, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.referredToMessage = referredToMessage;
        this.owner = owner;
        this.state = state;
        this.priority = priority;
        this.body = body;
        this.rating = rating;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Comment(Integer referredToMessage, User owner, String state, String priority,
                   String body, Integer rating, Timestamp createdAt, Timestamp updatedAt) {
        this(0, referredToMessage, owner, state, priority, body, rating, createdAt, updatedAt);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getReferredToMessage() {
        return referredToMessage;
    }

    public void setReferredToMessage(Integer referredToMessage) {
        this.referredToMessage = referredToMessage;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
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
        if (!(o instanceof Comment)) return false;
        Comment comment = (Comment) o;
        return Objects.equals(getId(), comment.getId()) &&
                Objects.equals(getReferredToMessage(), comment.getReferredToMessage()) &&
                Objects.equals(getOwner(), comment.getOwner()) &&
                Objects.equals(getState(), comment.getState()) &&
                Objects.equals(getPriority(), comment.getPriority()) &&
                Objects.equals(getBody(), comment.getBody()) &&
                Objects.equals(getRating(), comment.getRating()) &&
                Objects.equals(getCreatedAt(), comment.getCreatedAt()) &&
                Objects.equals(getUpdatedAt(), comment.getUpdatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getReferredToMessage(), getOwner(), getState(), getPriority(), getBody(), getRating(), getCreatedAt(), getUpdatedAt());
    }
}
