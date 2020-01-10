package net.thumbtack.forums.validator.user;

import org.apache.commons.validator.routines.EmailValidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EmailPatternValidator implements ConstraintValidator<EmailPattern, String> {
    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        return EmailValidator.getInstance().isValid(email);
    }
}
