package net.thumbtack.forums.model;

import net.thumbtack.forums.model.enums.MessageState;

import java.time.LocalDateTime;
import java.util.Objects;

public class HistoryItem {
    private int id;
    private String body;
    private MessageState state;
    private LocalDateTime createdAt;

    public HistoryItem(int id, String body, MessageState state, LocalDateTime createdAt) {
        this.id = id;
        this.body = body;
        this.state = state;
        this.createdAt = createdAt;
    }

    public HistoryItem(String body, MessageState state, LocalDateTime createdAt) {
        this(0, body, state, createdAt);
    }

    public HistoryItem(int id, String body, String state, LocalDateTime createdAt) {
        this(id, body, MessageState.valueOf(state), createdAt);
    }

    public HistoryItem(String body, String state, LocalDateTime createdAt) {
        this(0, body, MessageState.valueOf(state), createdAt);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
        return getId() == that.getId() &&
                Objects.equals(getBody(), that.getBody()) &&
                getState() == that.getState() &&
                Objects.equals(getCreatedAt(), that.getCreatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getBody(), getState(), getCreatedAt());
    }
}
