package net.thumbtack.forums.dto.message;

import net.thumbtack.forums.model.enums.MessagePriority;

import javax.validation.constraints.NotBlank;
import java.util.List;

public class MadeBranchFromCommentDtoRequest {
    @NotBlank
    private String subject;
    private MessagePriority priority;
    @NotBlank
    private List<String> tags;

    public MadeBranchFromCommentDtoRequest(String subject, MessagePriority priority, List<String> tags) {
        this.subject = subject;
        this.priority = priority;
        this.tags = tags;
    }

    public String getSubject() {
        return subject;
    }

    public MessagePriority getPriority() {
        return priority;
    }

    public List<String> getTags() {
        return tags;
    }
}
