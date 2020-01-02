package net.thumbtack.forums.validator.message;

import net.thumbtack.forums.model.enums.PublicationDecision;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.StringJoiner;

public class AvailableDecisionValidator implements ConstraintValidator<AvailableDecision, String> {
    private String message;

    @Override
    public void initialize(AvailableDecision constraintAnnotation) {
        final StringJoiner values = new StringJoiner(", ");
        for (PublicationDecision decision : PublicationDecision.values()) {
            values.add(decision.name());
        }
        message = String.format(constraintAnnotation.message(), values.toString());
    }

    @Override
    public boolean isValid(String decision, ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.disableDefaultConstraintViolation();
        constraintValidatorContext
                .buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();

        for (PublicationDecision instance : PublicationDecision.values()) {
            if (instance.name().equalsIgnoreCase(decision)) {
                return true;
            }
        }
        return false;
    }
}
