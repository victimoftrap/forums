package net.thumbtack.forums.model;

import net.thumbtack.forums.model.enums.ForumType;

import java.time.LocalDateTime;
import java.util.Objects;

public class Forum {
    private int id;
    private ForumType type;
    private User owner;
    private String name;
    private LocalDateTime createdAt;
    private boolean readonly;
    private int messageCount;
    private int commentCount;
    // REVU а еще можно список сообщений

    public Forum() {
    }

    public Forum(int id, ForumType type, User owner, String name, LocalDateTime createdAt,
                 boolean readonly, int messageCount, int commentCount) {
        this.id = id;
        this.type = type;
        this.owner = owner;
        this.name = name;
        this.createdAt = createdAt;
        this.readonly = readonly;
        this.messageCount = messageCount;
        this.commentCount = commentCount;
    }

    public Forum(ForumType type, User owner, String name, LocalDateTime createdAt,
                 boolean readonly, int messageCount, int commentCount) {
        this(0, type, owner, name, createdAt, readonly, messageCount, commentCount);
    }

    public Forum(int id, String type, User owner, String name, LocalDateTime createdAt,
                 boolean readonly, int messageCount, int commentCount) {
        this(id, ForumType.valueOf(type), owner, name, createdAt, readonly, messageCount, commentCount);
    }

    public Forum(String type, User owner, String name, LocalDateTime createdAt,
                 boolean readonly, int messageCount, int commentCount) {
        this(0, ForumType.valueOf(type), owner, name, createdAt, readonly, messageCount, commentCount);
    }

    public Forum(int id, ForumType type, User owner, String name, LocalDateTime createdAt, boolean readonly) {
        this(id, type, owner, name, createdAt, readonly, 0, 0);
    }

    public Forum(int id, String type, User owner, String name, LocalDateTime createdAt, boolean readonly) {
        this(id, ForumType.valueOf(type), owner, name, createdAt, readonly, 0, 0);
    }

    public Forum(ForumType type, User owner, String name, LocalDateTime createdAt) {
        this(0, type, owner, name, createdAt, false, 0, 0);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ForumType getType() {
        return type;
    }

    public void setType(ForumType type) {
        this.type = type;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Forum)) return false;
        Forum forum = (Forum) o;
        return getId() == forum.getId() &&
                isReadonly() == forum.isReadonly() &&
                getMessageCount() == forum.getMessageCount() &&
                getCommentCount() == forum.getCommentCount() &&
                getType() == forum.getType() &&
                Objects.equals(getOwner(), forum.getOwner()) &&
                Objects.equals(getName(), forum.getName()) &&
                Objects.equals(getCreatedAt(), forum.getCreatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getType(), getOwner(), getName(),
                getCreatedAt(), isReadonly(), getMessageCount(), getCommentCount()
        );
    }
}
