package net.thumbtack.forums.dto.requests.message;

import net.thumbtack.forums.validator.message.AvailableMessagePriority;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import java.util.List;

public class MadeBranchFromCommentDtoRequest {
    @NotBlank
    private String subject;

    @AvailableMessagePriority
    private String priority;

    private List<@NotBlank String> tags;

    @JsonCreator
    public MadeBranchFromCommentDtoRequest(@JsonProperty("subject") String subject,
                                           @JsonProperty("priority") String priority,
                                           @JsonProperty("tags") List<String> tags) {
        this.subject = subject;
        this.priority = priority;
        this.tags = tags;
    }

    public String getSubject() {
        return subject;
    }

    public String getPriority() {
        return priority;
    }

    public List<String> getTags() {
        return tags;
    }
}
