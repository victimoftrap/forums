package net.thumbtack.forums.dto.requests.message;

import net.thumbtack.forums.model.enums.MessagePriority;

import javax.validation.constraints.NotBlank;
import java.util.List;

public class CreateMessageDtoRequest {
    @NotBlank
    private String subject;
    @NotBlank
    private String body;
    @NotBlank
    private MessagePriority priority;
    @NotBlank
    private List<String> tags;

    public CreateMessageDtoRequest(String subject,
                                   String body,
                                   MessagePriority priority,
                                   List<String> tags) {
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

    public MessagePriority getPriority() {
        return priority;
    }

    public List<String> getTags() {
        return tags;
    }
}
