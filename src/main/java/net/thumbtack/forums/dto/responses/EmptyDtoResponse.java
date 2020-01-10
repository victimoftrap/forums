package net.thumbtack.forums.dto.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude
public class EmptyDtoResponse {
    @JsonCreator
    public EmptyDtoResponse() {
    }

    @Override
    public String toString() {
        return "{}";
    }
}
