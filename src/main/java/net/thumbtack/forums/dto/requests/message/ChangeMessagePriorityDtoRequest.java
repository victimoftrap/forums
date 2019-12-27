package net.thumbtack.forums.dto.requests.message;

import net.thumbtack.forums.model.enums.MessagePriority;

import javax.validation.constraints.NotBlank;

public class ChangeMessagePriorityDtoRequest {
    @NotBlank
    private MessagePriority priority;

    public ChangeMessagePriorityDtoRequest(MessagePriority priority) {
        this.priority = priority;
    }

    public MessagePriority getPriority() {
        return priority;
    }
}
