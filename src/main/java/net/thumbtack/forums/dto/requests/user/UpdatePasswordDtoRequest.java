package net.thumbtack.forums.dto.requests.user;

import net.thumbtack.forums.validator.user.UsernamePattern;
import net.thumbtack.forums.validator.user.PasswordPattern;

import javax.validation.constraints.NotBlank;

public class UpdatePasswordDtoRequest {
    @UsernamePattern
    private String name;
    @NotBlank
    private String oldPassword;
    @PasswordPattern
    private String password;

    public UpdatePasswordDtoRequest(String name, String oldPassword, String password) {
        this.name = name;
        this.oldPassword = oldPassword;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public String getPassword() {
        return password;
    }
}
