package net.thumbtack.forums.dto.message;

import net.thumbtack.forums.model.enums.MessagePriority;

public class ChangeMessagePriorityDtoRequest {
    private MessagePriority priority;

    public ChangeMessagePriorityDtoRequest(MessagePriority priority) {
        this.priority = priority;
    }

    public MessagePriority getPriority() {
        return priority;
    }
}
