package net.thumbtack.forums.validator.user;

import net.thumbtack.forums.configuration.ServerConfigurationProperties;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class UsernamePatternValidator implements ConstraintValidator<UsernamePattern, String> {
    private ServerConfigurationProperties props;
    private Pattern usernamePattern;
    private String message;

    @Autowired
    public void setProps(final ServerConfigurationProperties props) {
        this.props = props;
    }

    @Override
    public void initialize(UsernamePattern constraintAnnotation) {
        final String usernameRegex = "^[\\p{Alpha}\\p{Digit}\\p{IsCyrillic}]+$";
        usernamePattern = Pattern.compile(usernameRegex);

        message = String.format(constraintAnnotation.message(), props.getMaxNameLength());
    }

    @Override
    public boolean isValid(String username, ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.disableDefaultConstraintViolation();
        constraintValidatorContext
                .buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();

        return username != null
                && usernamePattern.matcher(username).matches()
                && username.length() < props.getMaxNameLength();
    }
}
