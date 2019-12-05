package net.thumbtack.forums.model;

import net.thumbtack.forums.model.enums.MessageState;

import java.time.LocalDateTime;
import java.util.Objects;

public class HistoryItem {
    private String body;
    private MessageState state;
    private LocalDateTime createdAt;

    public HistoryItem() {
    }

    public HistoryItem(String body, MessageState state, LocalDateTime createdAt) {
        this.body = body;
        this.state = state;
        this.createdAt = createdAt;
    }

    public HistoryItem(String body, String state, LocalDateTime createdAt) {
        this(body, MessageState.valueOf(state), createdAt);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public MessageState getState() {
        return state;
    }

    public void setState(MessageState state) {
        this.state = state;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HistoryItem)) return false;
        HistoryItem that = (HistoryItem) o;
        return Objects.equals(getBody(), that.getBody()) &&
                getState() == that.getState() &&
                Objects.equals(getCreatedAt(), that.getCreatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBody(), getState(), getCreatedAt());
    }
}
