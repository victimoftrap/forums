package net.thumbtack.forums.dto.requests.message;

import javax.validation.constraints.NotBlank;

public class PublicationDecisionDtoRequest {
    @NotBlank
    private PublicationDecision decision;

    public PublicationDecisionDtoRequest(PublicationDecision decision) {
        this.decision = decision;
    }

    public PublicationDecision getDecision() {
        return decision;
    }
}
