package net.thumbtack.forums.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.time.LocalDateTime;

public class MessageItem {
    private int id;
    private User owner;
    private MessageTree messageTree;
    private MessageItem parentMessage;
    private List<MessageItem> childrenComments;
    private List<HistoryItem> history;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private double averageRating;
    private int rated;

    public MessageItem() {
    }

    public MessageItem(int id, User owner, MessageTree messageTree, MessageItem parentMessage,
                       List<MessageItem> childrenComments, List<HistoryItem> history,
                       LocalDateTime createdAt, LocalDateTime updatedAt,
                       double averageRating, int rated) {
        this.id = id;
        this.owner = owner;
        this.messageTree = messageTree;
        this.parentMessage = parentMessage;
        this.childrenComments = childrenComments;
        this.history = history;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.averageRating = averageRating;
        this.rated = rated;
    }

    public MessageItem(User owner, MessageTree messageTree, MessageItem parentMessage,
                       List<MessageItem> childrenComments, List<HistoryItem> history,
                       LocalDateTime createdAt, LocalDateTime updatedAt,
                       double averageRating, int rated) {
        this(0, owner, messageTree, parentMessage, childrenComments,
                history, createdAt, updatedAt, averageRating, rated
        );
    }

    public MessageItem(int id, User owner, MessageTree messageTree, MessageItem parentMessage,
                       List<MessageItem> childrenComments, List<HistoryItem> history,
                       LocalDateTime createdAt, LocalDateTime updatedAt, double averageRating) {
        this(id, owner, messageTree, parentMessage, childrenComments,
                history, createdAt, updatedAt, averageRating, 0
        );
    }

    public MessageItem(User owner, MessageTree messageTree, MessageItem parentMessage,
                       List<MessageItem> childrenComments, List<HistoryItem> history,
                       LocalDateTime createdAt, LocalDateTime updatedAt, double averageRating) {
        this(0, owner, messageTree, parentMessage,
                childrenComments, history, createdAt, updatedAt,
                averageRating, 0
        );
    }

    public MessageItem(int id, User owner, MessageTree messageTree,
                       List<MessageItem> childrenComments, List<HistoryItem> history,
                       LocalDateTime createdAt, LocalDateTime updatedAt,
                       double averageRating, int  rated) {
        this(id, owner, messageTree, null, childrenComments,
                history, createdAt, updatedAt, averageRating, rated
        );
    }

    public MessageItem(User owner, MessageTree messageTree, MessageItem parentMessage,
                       List<MessageItem> childrenComments,
                       List<HistoryItem> history, LocalDateTime initialDate) {
        this(0, owner, messageTree, parentMessage, childrenComments,
                history, initialDate, initialDate, 0., 0
        );
    }

    public MessageItem(User owner, MessageTree messageTree, MessageItem parentMessage,
                       List<HistoryItem> history, LocalDateTime initialDate) {
        this(0, owner, messageTree, parentMessage,
                new ArrayList<>(), history, initialDate, initialDate,
                0., 0
        );
    }

    public MessageItem(User owner, List<HistoryItem> history, LocalDateTime initialDate) {
        this(0, owner, null, null,
                new ArrayList<>(), history, initialDate, initialDate,
                0., 0
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

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getRated() {
        return rated;
    }

    public void setRated(int rated) {
        this.rated = rated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageItem)) return false;
        MessageItem item = (MessageItem) o;
        return id == item.id &&
                averageRating == item.averageRating &&
                rated == item.rated &&
                Objects.equals(owner, item.owner) &&
                Objects.equals(messageTree, item.messageTree) &&
                Objects.equals(parentMessage, item.parentMessage) &&
                Objects.equals(childrenComments, item.childrenComments) &&
                Objects.equals(history, item.history) &&
                Objects.equals(createdAt, item.createdAt) &&
                Objects.equals(updatedAt, item.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, owner, messageTree, parentMessage,
                childrenComments, history, createdAt, updatedAt, averageRating, rated
        );
    }

    @Override
    public String toString() {
        return "MessageItem{" +
                "id=" + id +
                ", owner=" + owner +
                ", messageTree.id=" + getMessageTree().getId() +
                ", parentMessage=" + parentMessage +
                ", childrenComments=" + childrenComments +
                ", history=" + history +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", averageRating=" + averageRating +
                ", rated=" + rated +
                '}';
    }
}
