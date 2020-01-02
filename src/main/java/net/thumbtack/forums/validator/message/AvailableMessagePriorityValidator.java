package net.thumbtack.forums.validator.message;

import net.thumbtack.forums.model.enums.MessagePriority;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.StringJoiner;

public class AvailableMessagePriorityValidator implements ConstraintValidator<AvailableMessagePriority, String> {
    private String message;

    @Override
    public void initialize(AvailableMessagePriority constraintAnnotation) {
        final StringJoiner values = new StringJoiner(", ");
        for (MessagePriority priority : MessagePriority.values()) {
            values.add(priority.name());
        }
        message = String.format(constraintAnnotation.message(), values.toString());
    }

    @Override
    public boolean isValid(String priority, ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.disableDefaultConstraintViolation();
        constraintValidatorContext
                .buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();

        if (priority == null || "".equalsIgnoreCase(priority)) {
            return true;
        }

        for (MessagePriority instance : MessagePriority.values()) {
            if (instance.name().equalsIgnoreCase(priority)) {
                return true;
            }
        }
        return false;
    }
}
