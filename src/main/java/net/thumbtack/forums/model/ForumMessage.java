package net.thumbtack.forums.model;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class ForumMessage {
    private int id;
    private Forum forum;
    private User owner;
    private MessageStates state;
    private List<String> bodies;
    private int rating;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public ForumMessage(int id, Forum forum, User owner, MessageStates state,
                        List<String> bodies, int rating, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.forum = forum;
        this.owner = owner;
        this.state = state;
        this.bodies = bodies;
        this.rating = rating;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public ForumMessage(Forum forum, User owner, MessageStates state,
                        List<String> bodies, int rating, Timestamp createdAt, Timestamp updatedAt) {
        this(0, forum, owner, state, bodies, rating, createdAt, updatedAt);
    }

    public ForumMessage(int id, Forum forum, User owner, MessageStates state,
                        String body, int rating, Timestamp createdAt, Timestamp updatedAt) {
        this(id, forum, owner, state, Collections.singletonList(body),
                rating, createdAt, updatedAt
        );
    }

    public ForumMessage(Forum forum, User owner, MessageStates state,
                        String body, int rating, Timestamp createdAt, Timestamp updatedAt) {
        this(0, forum, owner, state, body, rating, createdAt, updatedAt);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public List<String> getBodies() {
        return bodies;
    }

    public void setBodies(List<String> bodies) {
        this.bodies = bodies;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
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
        if (!(o instanceof ForumMessage)) return false;
        ForumMessage that = (ForumMessage) o;
        return getId() == that.getId() &&
                getRating() == that.getRating() &&
                Objects.equals(getForum(), that.getForum()) &&
                Objects.equals(getOwner(), that.getOwner()) &&
                getState() == that.getState() &&
                Objects.equals(getBodies(), that.getBodies()) &&
                Objects.equals(getCreatedAt(), that.getCreatedAt()) &&
                Objects.equals(getUpdatedAt(), that.getUpdatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getForum(), getOwner(), getState(),
                getBodies(), getRating(), getCreatedAt(), getUpdatedAt()
        );
    }
}
