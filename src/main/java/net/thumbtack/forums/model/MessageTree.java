package net.thumbtack.forums.model;

import net.thumbtack.forums.model.enums.MessagePriority;

import java.util.List;
import java.util.Objects;

public class MessageTree {
    private int id;
    private Forum forum;
    private String subject;
    private MessageItem rootMessage;
    private MessagePriority priority;
    private List<Tag> tags;

    public MessageTree() {
    }

    public MessageTree(int id, Forum forum, String subject, MessageItem rootMessage,
                       MessagePriority priority, List<Tag> tags) {
        this.id = id;
        this.forum = forum;
        this.subject = subject;
        this.rootMessage = rootMessage;
        this.priority = priority;
        this.tags = tags;
    }

    public MessageTree(Forum forum, String subject, MessageItem rootMessage,
                       MessagePriority priority, List<Tag> tags) {
        this(0, forum, subject, rootMessage, priority, tags);
    }

    public MessageTree(int id, Forum forum, String subject, MessageItem rootMessage,
                       String priority, List<Tag> tags) {
        this(id, forum, subject, rootMessage, MessagePriority.valueOf(priority), tags);
    }

    public MessageTree(Forum forum, String subject, MessageItem rootMessage,
                       String priority, List<Tag> tags) {
        this(0, forum, subject, rootMessage, MessagePriority.valueOf(priority), tags);
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
        MessageTree that = (MessageTree) o;
        return getId() == that.getId() &&
                Objects.equals(getForum(), that.getForum()) &&
                Objects.equals(getSubject(), that.getSubject()) &&
                Objects.equals(getRootMessage(), that.getRootMessage()) &&
                getPriority() == that.getPriority() &&
                Objects.equals(getTags(), that.getTags());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getForum(), getSubject(), getRootMessage(), getPriority(), getTags());
    }
}
