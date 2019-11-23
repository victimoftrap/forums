package net.thumbtack.forums.dto;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

public class LoginUserDtoRequest {
    @NotBlank
    private String name;
    @NotBlank
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
