package net.thumbtack.forums.dto.message;

import javax.validation.constraints.NotBlank;

public class EditMessageOrCommentDtoRequest {
    @NotBlank
    private String body;

    public EditMessageOrCommentDtoRequest(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }
}
