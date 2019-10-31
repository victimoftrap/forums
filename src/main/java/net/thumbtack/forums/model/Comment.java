package net.thumbtack.forums.model;

import java.sql.Timestamp;
import java.util.Objects;

public class Comment {
    private Integer id;
    private Forum forum;
    private User owner;
    private Message referredMessage;
    private MessageStates state;
    private String body;
    private Integer rating;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Comment(Integer id, Forum forum, User owner, Message referredMessage,
                   MessageStates state, String body, Integer rating,
                   Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.forum = forum;
        this.owner = owner;
        this.referredMessage = referredMessage;
        this.state = state;
        this.body = body;
        this.rating = rating;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Comment(Integer id, Forum forum, User owner, Message referredMessage,
                   String state, String body, Integer rating,
                   Timestamp createdAt, Timestamp updatedAt) {
        this(id, forum, owner, referredMessage, MessageStates.valueOf(state),
                body, rating, createdAt, updatedAt
        );
    }

    public Comment(Forum forum, User owner, Message referredMessage,
                   String state, String body, Integer rating,
                   Timestamp createdAt, Timestamp updatedAt) {
        this(0, forum, owner, referredMessage, state, body, rating, createdAt, updatedAt);
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

    public Message getReferredMessage() {
        return referredMessage;
    }

    public void setReferredMessage(Message referredMessage) {
        this.referredMessage = referredMessage;
    }

    public MessageStates getState() {
        return state;
    }

    public void setState(MessageStates state) {
        this.state = state;
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
                Objects.equals(getForum(), comment.getForum()) &&
                Objects.equals(getOwner(), comment.getOwner()) &&
                Objects.equals(getReferredMessage(), comment.getReferredMessage()) &&
                getState() == comment.getState() &&
                Objects.equals(getBody(), comment.getBody()) &&
                Objects.equals(getRating(), comment.getRating()) &&
                Objects.equals(getCreatedAt(), comment.getCreatedAt()) &&
                Objects.equals(getUpdatedAt(), comment.getUpdatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getForum(), getOwner(), getReferredMessage(),
                getState(), getBody(), getRating(), getCreatedAt(), getUpdatedAt()
        );
    }
}
