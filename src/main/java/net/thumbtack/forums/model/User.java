package net.thumbtack.forums.model;

import net.thumbtack.forums.model.enums.UserRole;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

public class User {
    private int id;
    private UserRole role;
    private String username;
    private String email;
    private String password;
    private LocalDateTime registeredAt;
    private boolean deleted;
    private LocalDateTime bannedUntil;
    private int banCount;
    // REVU а еще можно список сообщений и список рейтингов
    // но на усмотрение

    public User() {
    }

    public User(int id, UserRole role, String username, String email, String password,
                LocalDateTime registeredAt, boolean deleted, LocalDateTime bannedUntil, int banCount) {
        this.id = id;
        this.role = role;
        this.username = username;
        this.email = email;
        this.password = password;
        this.deleted = deleted;
        this.registeredAt = registeredAt;
        this.bannedUntil = bannedUntil;
        this.banCount = banCount;
    }

    public User(UserRole role, String username, String email, String password,
                LocalDateTime registeredAt, boolean deleted, LocalDateTime bannedUntil, int banCount) {
        this(0, role, username, email, password, registeredAt, deleted, bannedUntil, banCount);
    }

    public User(int id, UserRole role, String username,
                String email, String password, LocalDateTime registeredAt, boolean deleted) {
        this(id, role, username, email, password, registeredAt, deleted, null, 0);
    }

    public User(UserRole role, String username,
                String email, String password, LocalDateTime registeredAt, boolean deleted) {
        this(0, role, username, email, password, registeredAt, deleted, null, 0);
    }

    public User(int id, UserRole role, String username, String email, String password,
                Timestamp registeredAt, boolean deleted, Timestamp bannedUntil, int banCount) {
        this(id, role, username, email, password,
                registeredAt.toLocalDateTime(), deleted,
                bannedUntil.toLocalDateTime(), banCount
        );
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public LocalDateTime getBannedUntil() {
        return bannedUntil;
    }

    public void setBannedUntil(LocalDateTime bannedUntil) {
        this.bannedUntil = bannedUntil;
    }

    public int getBanCount() {
        return banCount;
    }

    public void setBanCount(int banCount) {
        this.banCount = banCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return getId() == user.getId() &&
                isDeleted() == user.isDeleted() &&
                getBanCount() == user.getBanCount() &&
                getRole() == user.getRole() &&
                Objects.equals(getUsername(), user.getUsername()) &&
                Objects.equals(getEmail(), user.getEmail()) &&
                Objects.equals(getPassword(), user.getPassword()) &&
                Objects.equals(getRegisteredAt(), user.getRegisteredAt()) &&
                Objects.equals(getBannedUntil(), user.getBannedUntil());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getRole(), getUsername(), getEmail(), getPassword(),
                isDeleted(), getRegisteredAt(), getBannedUntil(), getBanCount()
        );
    }
}
