package net.thumbtack.forums.model;

import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class Forum {
    private int id;
    private ForumTypes type;
    private User owner;
    private String name;
    private Timestamp createdAt;
    private boolean readonly;
    private List<Message> messages;

    public Forum(int id, ForumTypes type, User owner, String name,
                 Timestamp createdAt, boolean readonly, List<Message> messages) {
        this.id = id;
        this.type = type;
        this.owner = owner;
        this.name = name;
        this.createdAt = createdAt;
        this.readonly = readonly;
        this.messages = messages;
    }

    public Forum(int id, String type, User owner, String name,
                 Timestamp createdAt, boolean readonly, List<Message> messages) {
        this(id, ForumTypes.valueOf(type), owner, name, createdAt, readonly, messages);
    }

    public Forum(String type, User owner, String name,
                 Timestamp createdAt, boolean readonly, List<Message> messages) {
        this(0, type, owner, name, createdAt, readonly, messages);
    }

    public Forum(int id, String type, User owner, String name, Timestamp createdAt, boolean readonly) {
        this(id, type, owner, name, createdAt, readonly, new ArrayList<>());
    }

    public Forum(String type, User owner, String name, Timestamp createdAt, boolean readonly) {
        this(0, type, owner, name, createdAt, readonly, new ArrayList<>());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public boolean getReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
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
        return getId() == forum.getId() &&
                getReadonly() == forum.getReadonly() &&
                getType() == forum.getType() &&
                Objects.equals(getOwner(), forum.getOwner()) &&
                Objects.equals(getName(), forum.getName()) &&
                Objects.equals(getCreatedAt(), forum.getCreatedAt()) &&
                Objects.equals(getMessages(), forum.getMessages());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getType(), getOwner(),
                getName(), getCreatedAt(), getReadonly(), getMessages()
        );
    }
}
