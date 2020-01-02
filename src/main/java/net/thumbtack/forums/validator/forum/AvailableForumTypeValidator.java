package net.thumbtack.forums.validator.forum;

import net.thumbtack.forums.model.enums.ForumType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.StringJoiner;

public class AvailableForumTypeValidator implements ConstraintValidator<AvailableForumType, String> {
    private String message;

    @Override
    public void initialize(AvailableForumType constraintAnnotation) {
        final StringJoiner values = new StringJoiner(", ");
        for (ForumType decision : ForumType.values()) {
            values.add(decision.name());
        }
        message = String.format(constraintAnnotation.message(), values.toString());
    }

    @Override
    public boolean isValid(String forumType, ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.disableDefaultConstraintViolation();
        constraintValidatorContext
                .buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();

        for (ForumType instance : ForumType.values()) {
            if (instance.name().equalsIgnoreCase(forumType)) {
                return true;
            }
        }
        return false;
    }
}
