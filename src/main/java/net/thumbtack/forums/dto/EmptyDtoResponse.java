package net.thumbtack.forums.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude
public class EmptyDtoResponse {
    @JsonCreator
    public EmptyDtoResponse() {
    }
}
