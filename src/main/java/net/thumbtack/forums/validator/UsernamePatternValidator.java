package net.thumbtack.forums.validator;

import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UsernamePatternValidator implements ConstraintValidator<UsernamePattern, String> {
    private Pattern usernamePattern;

    @Override
    public void initialize(UsernamePattern constraintAnnotation) {
        final String usernameRegex = "^[\\p{Alpha}\\p{Digit}\\p{IsCyrillic}]+$";
        usernamePattern = Pattern.compile(usernameRegex);
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return usernamePattern.matcher(s).matches();
    }
}
