package net.thumbtack.forums.view;

import java.util.Objects;

public class MessagesCountView {
    private int messagesCount;
    private int commentsCount;

    public MessagesCountView(int messagesCount, int commentsCount) {
        this.messagesCount = messagesCount;
        this.commentsCount = commentsCount;
    }

    public int getMessagesCount() {
        return messagesCount;
    }

    public void setMessagesCount(int messagesCount) {
        this.messagesCount = messagesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessagesCountView)) return false;
        MessagesCountView that = (MessagesCountView) o;
        return messagesCount == that.messagesCount &&
                commentsCount == that.commentsCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(messagesCount, commentsCount);
    }

    @Override
    public String toString() {
        return "MessagesCountView{" +
                "messagesCount=" + messagesCount +
                ", commentsCount=" + commentsCount +
                '}';
    }
}
