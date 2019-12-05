package net.thumbtack.forums.dto.message;

import javax.validation.constraints.NotBlank;

public class CreateCommentDtoRequest {
    @NotBlank
    private String body;

    public CreateCommentDtoRequest(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }
}
