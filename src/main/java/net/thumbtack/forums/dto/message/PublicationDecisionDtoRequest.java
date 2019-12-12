package net.thumbtack.forums.dto.message;

public class PublicationDecisionDtoRequest {
    private PublicationDecision decision;

    public PublicationDecisionDtoRequest(PublicationDecision decision) {
        this.decision = decision;
    }

    public PublicationDecision getDecision() {
        return decision;
    }
}
