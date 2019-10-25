package net.thumbtack.forums.model;

import java.sql.Timestamp;
import java.util.Objects;

public class User {
    private Integer id;
    private UserRoles role;
    private String userName;
    private String email;
    private String password;
    private Timestamp registeredAt;
    private Timestamp bannedUntil;
    private Integer banCount;
    private Boolean arePermanent;

    public User(Integer id, UserRoles role, String username, String email, String password,
                Timestamp registeredAt, Timestamp bannedUntil,
                Integer banCount, Boolean arePermanent) {
        this.id = id;
        this.role = role;
        this.userName = username;
        this.email = email;
        this.password = password;
        this.registeredAt = registeredAt;
        this.bannedUntil = bannedUntil;
        this.banCount = banCount;
        this.arePermanent = arePermanent;
    }

    public User(UserRoles role, String username, String email, String password, Timestamp registeredAt,
                Timestamp bannedUntil, Integer banCount, Boolean arePermanent) {
        this(0, role, username, email, password,
                registeredAt, bannedUntil, banCount, arePermanent
        );
    }

    public User(Integer id, UserRoles role, String username, String email,
                String password, Timestamp registeredAt) {
        this(id, role, username, email, password,
                registeredAt, null, 0, false
        );
    }

    public User(UserRoles role, String username, String email,
                String password, Timestamp registeredAt) {
        this(0, role, username, email, password,
                registeredAt, null, 0, false
        );
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UserRoles getRole() {
        return role;
    }

    public void setRole(UserRoles role) {
        this.role = role;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public Timestamp getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Timestamp registeredAt) {
        this.registeredAt = registeredAt;
    }

    public Timestamp getBannedUntil() {
        return bannedUntil;
    }

    public void setBannedUntil(Timestamp bannedUntil) {
        this.bannedUntil = bannedUntil;
    }

    public Integer getBanCount() {
        return banCount;
    }

    public void setBanCount(Integer banCount) {
        this.banCount = banCount;
    }

    public Boolean getArePermanent() {
        return arePermanent;
    }

    public void setArePermanent(Boolean arePermanent) {
        this.arePermanent = arePermanent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(getId(), user.getId()) &&
                getRole() == user.getRole() &&
                Objects.equals(getUserName(), user.getUserName()) &&
                Objects.equals(getEmail(), user.getEmail()) &&
                Objects.equals(getPassword(), user.getPassword()) &&
                Objects.equals(getRegisteredAt(), user.getRegisteredAt()) &&
                Objects.equals(getBannedUntil(), user.getBannedUntil()) &&
                Objects.equals(getBanCount(), user.getBanCount()) &&
                Objects.equals(getArePermanent(), user.getArePermanent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getId(), getRole(), getUserName(), getEmail(), getPassword(),
                getRegisteredAt(), getBannedUntil(), getBanCount(), getArePermanent()
        );
    }
}
