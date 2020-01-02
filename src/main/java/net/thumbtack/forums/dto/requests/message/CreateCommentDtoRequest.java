package net.thumbtack.forums.dto.requests.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;

public class CreateCommentDtoRequest {
    @NotBlank
    private String body;

    @JsonCreator
    public CreateCommentDtoRequest(@JsonProperty("body") String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }
}
