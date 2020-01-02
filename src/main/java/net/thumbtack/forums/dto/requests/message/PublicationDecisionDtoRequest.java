package net.thumbtack.forums.dto.requests.message;

import net.thumbtack.forums.validator.message.AvailableDecision;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PublicationDecisionDtoRequest {
    @AvailableDecision
    private String decision;

    @JsonCreator
    public PublicationDecisionDtoRequest(@JsonProperty("decision") String decision) {
        this.decision = decision;
    }

    public String getDecision() {
        return decision;
    }
}
