package net.thumbtack.forums.dto.requests.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.thumbtack.forums.validator.message.AvailableRating;

public class RateMessageDtoRequest {
    @AvailableRating
    private Integer value;

    @JsonCreator
    public RateMessageDtoRequest(@JsonProperty("value") Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
