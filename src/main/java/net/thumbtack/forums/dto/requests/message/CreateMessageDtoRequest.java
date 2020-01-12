package net.thumbtack.forums.dto.requests.message;

import net.thumbtack.forums.validator.message.AvailableMessagePriority;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import java.util.List;

public class CreateMessageDtoRequest {
    @NotBlank
    private String subject;

    @NotBlank
    private String body;

    @AvailableMessagePriority
    private String priority;

    private List<@NotBlank String> tags;

    @JsonCreator
    public CreateMessageDtoRequest(@JsonProperty("subject") String subject,
                                   @JsonProperty("body") String body,
                                   @JsonProperty("priority") String priority,
                                   @JsonProperty("tags") List<String> tags) {
        this.subject = subject;
        this.body = body;
        this.priority = priority;
        this.tags = tags;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getPriority() {
        return priority;
    }

    public List<String> getTags() {
        return tags;
    }
}
