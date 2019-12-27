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
    private UserStatus status;
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
            @JsonProperty("status") UserStatus status,
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

    public UserStatus getStatus() {
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
        return getId() == that.getId() &&
                isOnline() == that.isOnline() &&
                isDeleted() == that.isDeleted() &&
                getBanCount() == that.getBanCount() &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getEmail(), that.getEmail()) &&
                Objects.equals(getTimeRegistered(), that.getTimeRegistered()) &&
                Objects.equals(isSuper(), that.isSuper()) &&
                getStatus() == that.getStatus() &&
                Objects.equals(getTimeBanExit(), that.getTimeBanExit());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getEmail(), getTimeRegistered(),
                isOnline(), isDeleted(), isSuper(), getStatus(),
                getTimeBanExit(), getBanCount()
        );
    }
}
