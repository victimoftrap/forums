package net.thumbtack.forums.validator.message;

import net.thumbtack.forums.model.enums.MessageOrder;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;
import java.util.StringJoiner;

public class AvailableOrderValidator implements ConstraintValidator<AvailableOrder, String> {
    private String message;

    @Override
    public void initialize(AvailableOrder constraintAnnotation) {
        final StringJoiner values = new StringJoiner(", ");
        for (MessageOrder decision : MessageOrder.values()) {
            values.add(decision.name());
        }
        message = String.format(constraintAnnotation.message(), values.toString());
    }

    @Override
    public boolean isValid(String order, ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.disableDefaultConstraintViolation();
        constraintValidatorContext
                .buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();

        if (order == null) {
            return true;
        }
        for (MessageOrder instance : MessageOrder.values()) {
            if (instance.name().equalsIgnoreCase(order)) {
                return true;
            }
        }
        return false;
    }
}
