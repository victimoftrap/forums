package net.thumbtack.forums.dto;

import javax.validation.constraints.NotBlank;

public class UpdatePasswordDtoRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String oldPassword;
    @NotBlank
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
