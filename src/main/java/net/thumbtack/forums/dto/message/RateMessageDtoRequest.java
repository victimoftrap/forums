package net.thumbtack.forums.dto.message;

public class RateMessageDtoRequest {
    private Integer value;

    public RateMessageDtoRequest(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
