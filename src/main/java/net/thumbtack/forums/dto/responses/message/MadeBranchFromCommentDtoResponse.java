package net.thumbtack.forums.dto.responses.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MadeBranchFromCommentDtoResponse {
    private int id;

    @JsonCreator
    public MadeBranchFromCommentDtoResponse(@JsonProperty("id") int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
