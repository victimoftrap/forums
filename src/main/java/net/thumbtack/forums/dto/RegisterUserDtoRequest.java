package net.thumbtack.forums.dto;

import net.thumbtack.forums.validator.UsernamePattern;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

public class RegisterUserDtoRequest {
    @UsernamePattern
    private String name;
    @NotBlank
    private String email;
    @NotBlank
    private String password;

    public RegisterUserDtoRequest() {
    }

    public RegisterUserDtoRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegisterUserDtoRequest)) return false;
        RegisterUserDtoRequest that = (RegisterUserDtoRequest) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getEmail(), that.getEmail()) &&
                Objects.equals(getPassword(), that.getPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getEmail(), getPassword());
    }
}
