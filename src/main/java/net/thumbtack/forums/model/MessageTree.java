package net.thumbtack.forums.model;

import net.thumbtack.forums.model.enums.MessagePriority;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MessageTree {
    private int id;
    private Forum forum;
    private String subject;
    private MessageItem rootMessage;
    private MessagePriority priority;
    private LocalDateTime createdAt;
    private List<Tag> tags;

    public MessageTree() {
    }

    public MessageTree(int id, Forum forum, String subject, MessageItem rootMessage,
                       MessagePriority priority, LocalDateTime createdAt, List<Tag> tags) {
        this.id = id;
        this.forum = forum;
        this.subject = subject;
        this.rootMessage = rootMessage;
        this.priority = priority;
        this.createdAt = createdAt;
        this.tags = tags;
    }

    public MessageTree(Forum forum, String subject, MessageItem rootMessage,
                       MessagePriority priority, LocalDateTime createdAt, List<Tag> tags) {
        this(0, forum, subject, rootMessage, priority, createdAt, tags);
    }

    public MessageTree(int id, Forum forum, String subject, MessageItem rootMessage,
                       MessagePriority priority, LocalDateTime createdAt) {
        this(id, forum, subject, rootMessage, priority, createdAt, new ArrayList<>());
    }

    public MessageTree(int id, Forum forum, String subject, MessageItem rootMessage,
                       String priority, LocalDateTime createdAt, List<Tag> tags) {
        this(id, forum, subject, rootMessage, MessagePriority.valueOf(priority), createdAt, tags);
    }

    public MessageTree(Forum forum, String subject, MessageItem rootMessage,
                       String priority, LocalDateTime createdAt, List<Tag> tags) {
        this(0, forum, subject, rootMessage, MessagePriority.valueOf(priority), createdAt, tags);
    }

    public MessageTree(Forum forum, String subject, MessageItem rootMessage,
                       MessagePriority priority, LocalDateTime createdAt) {
        this(0, forum, subject, rootMessage, priority, createdAt, new ArrayList<>());
    }

    public MessageTree(int id, Forum forum, String subject, MessageItem rootMessage, MessagePriority priority) {
        this(id, forum, subject, rootMessage, priority,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), new ArrayList<>()
        );
    }

    public MessageTree(Forum forum, String subject, MessageItem rootMessage, MessagePriority priority) {
        this(0, forum, subject, rootMessage, priority,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), new ArrayList<>()
        );
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public MessageItem getRootMessage() {
        return rootMessage;
    }

    public void setRootMessage(MessageItem rootMessage) {
        this.rootMessage = rootMessage;
    }

    public MessagePriority getPriority() {
        return priority;
    }

    public void setPriority(MessagePriority priority) {
        this.priority = priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageTree)) return false;
        MessageTree tree = (MessageTree) o;
        return id == tree.id &&
                Objects.equals(forum, tree.forum) &&
                Objects.equals(subject, tree.subject) &&
                Objects.equals(rootMessage, tree.rootMessage) &&
                priority == tree.priority &&
                Objects.equals(createdAt, tree.createdAt) &&
                Objects.equals(tags, tree.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, forum, subject, rootMessage, priority, createdAt, tags);
    }

    @Override
    public String toString() {
        return "MessageTree{" +
                "id=" + id +
                ", forum=" + forum +
                ", subject='" + subject + '\'' +
                ", rootMessage.id=" + rootMessage +
                ", priority=" + priority +
                ", createdAt=" + createdAt +
                ", tags=" + tags +
                '}';
    }
}
