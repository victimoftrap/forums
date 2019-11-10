package net.thumbtack.forums.model;

import net.thumbtack.forums.model.enums.UserRole;

import java.time.LocalDateTime;
import java.util.Objects;

public class User {
    private int id;
    private UserRole role;
    private String username;
    private String email;
    private String password;
    private boolean areDeleted;
    private LocalDateTime registeredAt;
    private LocalDateTime bannedUntil;
    private int banCount;

    public User(int id, UserRole role, String username, String email, String password,
                boolean areDeleted, LocalDateTime registeredAt, LocalDateTime bannedUntil, int banCount) {
        this.id = id;
        this.role = role;
        this.username = username;
        this.email = email;
        this.password = password;
        this.areDeleted = areDeleted;
        this.registeredAt = registeredAt;
        this.bannedUntil = bannedUntil;
        this.banCount = banCount;
    }

    public User(UserRole role, String username, String email, String password,
                boolean areDeleted, LocalDateTime registeredAt, LocalDateTime bannedUntil, int banCount) {
        this(0, role, username, email, password, areDeleted, registeredAt, bannedUntil, banCount);
    }

    public User(int id, UserRole role, String username,
                String email, String password, boolean areDeleted, LocalDateTime registeredAt) {
        this(id, role, username, email, password, areDeleted, registeredAt, null, 0);
    }

    public User(int id, String role, String username, String email, String password,
                boolean areDeleted, LocalDateTime registeredAt, LocalDateTime bannedUntil, int banCount) {
        this(id, UserRole.valueOf(role), username, email, password,
                areDeleted, registeredAt, bannedUntil, banCount
        );
    }

    public User(String role, String username, String email, String password,
                boolean areDeleted, LocalDateTime registeredAt, LocalDateTime bannedUntil, int banCount) {
        this(0, UserRole.valueOf(role), username, email, password,
                areDeleted, registeredAt, bannedUntil, banCount
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

    public boolean isAreDeleted() {
        return areDeleted;
    }

    public void setAreDeleted(boolean areDeleted) {
        this.areDeleted = areDeleted;
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
                isAreDeleted() == user.isAreDeleted() &&
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
                isAreDeleted(), getRegisteredAt(), getBannedUntil(), getBanCount()
        );
    }
}
