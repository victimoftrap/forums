package net.thumbtack.forums.dto.requests.message;

import net.thumbtack.forums.validator.message.AvailableMessagePriority;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ChangeMessagePriorityDtoRequest {
    @AvailableMessagePriority
    private String priority;

    @JsonCreator
    public ChangeMessagePriorityDtoRequest(@JsonProperty("priority") String priority) {
        this.priority = priority;
    }

    public String getPriority() {
        return priority;
    }
}
