package net.thumbtack.forums.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class MessageItem {
    private int id;
    private User owner;
    private MessageTree messageTree;
    private MessageItem parentMessage;
    private List<MessageItem> childrenComments;
    private List<HistoryItem> history;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int rating;

    public MessageItem() {
    }

    public MessageItem(int id, User owner, MessageTree messageTree, MessageItem parentMessage,
                       List<MessageItem> childrenComments, List<HistoryItem> history,
                       LocalDateTime createdAt, LocalDateTime updatedAt, int rating) {
        this.id = id;
        this.owner = owner;
        this.messageTree = messageTree;
        this.parentMessage = parentMessage;
        this.childrenComments = childrenComments;
        this.history = history;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.rating = rating;
    }

    public MessageItem(User owner, MessageTree messageTree, MessageItem parentMessage,
                       List<MessageItem> childrenComments, List<HistoryItem> history,
                       LocalDateTime createdAt, LocalDateTime updatedAt, int rating) {
        this(0, owner, messageTree, parentMessage,
                childrenComments, history, createdAt, updatedAt, rating
        );
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public MessageTree getMessageTree() {
        return messageTree;
    }

    public void setMessageTree(MessageTree messageTree) {
        this.messageTree = messageTree;
    }

    public MessageItem getParentMessage() {
        return parentMessage;
    }

    public void setParentMessage(MessageItem parentMessage) {
        this.parentMessage = parentMessage;
    }

    public List<MessageItem> getChildrenComments() {
        return childrenComments;
    }

    public void setChildrenComments(List<MessageItem> childrenComments) {
        this.childrenComments = childrenComments;
    }

    public List<HistoryItem> getHistory() {
        return history;
    }

    public void setHistory(List<HistoryItem> history) {
        this.history = history;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageItem)) return false;
        MessageItem that = (MessageItem) o;
        return getId() == that.getId() &&
                getRating() == that.getRating() &&
                Objects.equals(getOwner(), that.getOwner()) &&
                Objects.equals(getMessageTree(), that.getMessageTree()) &&
                Objects.equals(getParentMessage(), that.getParentMessage()) &&
                Objects.equals(getChildrenComments(), that.getChildrenComments()) &&
                Objects.equals(getHistory(), that.getHistory()) &&
                Objects.equals(getCreatedAt(), that.getCreatedAt()) &&
                Objects.equals(getUpdatedAt(), that.getUpdatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getOwner(), getMessageTree(), getParentMessage(),
                getChildrenComments(), getHistory(), getCreatedAt(), getUpdatedAt(), getRating()
        );
    }
}
