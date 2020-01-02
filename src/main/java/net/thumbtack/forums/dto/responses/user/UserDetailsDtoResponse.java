package net.thumbtack.forums.dto.responses.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailsDtoResponse {
    private int id;
    private String name;
    private String email;
    private LocalDateTime timeRegistered;
    private boolean online;
    private boolean deleted;
    private Boolean isSuper;
    private String status;
    private LocalDateTime timeBanExit;
    private int banCount;

    @JsonCreator
    public UserDetailsDtoResponse(
            @JsonProperty("id") int id,
            @JsonProperty("name") String name,
            @JsonProperty("email") String email,
            @JsonProperty("timeRegistered") LocalDateTime timeRegistered,
            @JsonProperty("online") boolean online,
            @JsonProperty("deleted") boolean deleted,
            @JsonProperty("super") Boolean isSuper,
            @JsonProperty("status") String status,
            @JsonProperty("timeBanExit") LocalDateTime timeBanExit,
            @JsonProperty("banCount") int banCount) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.timeRegistered = timeRegistered;
        this.online = online;
        this.deleted = deleted;
        this.isSuper = isSuper;
        this.status = status;
        this.timeBanExit = timeBanExit;
        this.banCount = banCount;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getTimeRegistered() {
        return timeRegistered;
    }

    public boolean isOnline() {
        return online;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Boolean isSuper() {
        return isSuper;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getTimeBanExit() {
        return timeBanExit;
    }

    public int getBanCount() {
        return banCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDetailsDtoResponse)) return false;
        UserDetailsDtoResponse that = (UserDetailsDtoResponse) o;
        return id == that.id &&
                online == that.online &&
                deleted == that.deleted &&
                banCount == that.banCount &&
                Objects.equals(name, that.name) &&
                Objects.equals(email, that.email) &&
                Objects.equals(timeRegistered, that.timeRegistered) &&
                Objects.equals(isSuper, that.isSuper) &&
                Objects.equals(status, that.status) &&
                Objects.equals(timeBanExit, that.timeBanExit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, timeRegistered,
                online, deleted, isSuper, status, timeBanExit, banCount
        );
    }
}
