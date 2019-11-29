package net.thumbtack.forums.dto.user;

import net.thumbtack.forums.validator.PasswordPattern;
import net.thumbtack.forums.validator.UsernamePattern;

import java.util.Objects;

public class LoginUserDtoRequest {
    @UsernamePattern
    private String name;
    @PasswordPattern
    private String password;

    public LoginUserDtoRequest(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoginUserDtoRequest)) return false;
        LoginUserDtoRequest that = (LoginUserDtoRequest) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getPassword(), that.getPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getPassword());
    }
}
