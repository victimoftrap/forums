package net.thumbtack.forums.dto.requests.message;

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
