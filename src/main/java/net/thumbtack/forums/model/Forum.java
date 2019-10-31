package net.thumbtack.forums.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Forum {
    private Integer id;
    private ForumTypes type;
    private User owner;
    private String name;
    private Timestamp createdAt;
    private Boolean readonly;
    private List<Message> messages;

    public Forum(Integer id, ForumTypes type, User owner, String name,
                 Timestamp createdAt, Boolean readonly, List<Message> messages) {
        this.id = id;
        this.type = type;
        this.owner = owner;
        this.name = name;
        this.createdAt = createdAt;
        this.readonly = readonly;
        this.messages = messages;
    }

    public Forum(Integer id, String type, User owner,
                 String name, Timestamp createdAt, Boolean readonly, List<Message> messages) {
        this(id, ForumTypes.valueOf(type), owner, name, createdAt, readonly, messages);
    }

    public Forum(String type, User owner, String name,
                 Timestamp createdAt, Boolean readonly, List<Message> messages) {
        this(0, type, owner, name, createdAt, readonly, messages);
    }

    public Forum(Integer id, String type, User owner, String name,
                 Timestamp createdAt, Boolean readonly) {
        this(id, type, owner, name, createdAt, readonly, new ArrayList<>());
    }

    public Forum(String type, User owner, String name,
                 Timestamp createdAt, Boolean readonly) {
        this(0, type, owner, name, createdAt, readonly, new ArrayList<>());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ForumTypes getType() {
        return type;
    }

    public void setType(ForumTypes type) {
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getReadonly() {
        return readonly;
    }

    public void setReadonly(Boolean readonly) {
        this.readonly = readonly;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Forum)) return false;
        Forum forum = (Forum) o;
        return Objects.equals(getId(), forum.getId()) &&
                getType() == forum.getType() &&
                Objects.equals(getOwner(), forum.getOwner()) &&
                Objects.equals(getName(), forum.getName()) &&
                Objects.equals(getCreatedAt(), forum.getCreatedAt()) &&
                Objects.equals(getReadonly(), forum.getReadonly()) &&
                Objects.equals(getMessages(), forum.getMessages());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getType(), getOwner(), getName(),
                getCreatedAt(), getReadonly(), getMessages()
        );
    }
}
