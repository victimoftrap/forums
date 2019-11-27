package net.thumbtack.forums.validator;

import net.thumbtack.forums.configuration.ServerConfigurationProperties;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class PasswordPatternValidator implements ConstraintValidator<PasswordPattern, String> {
    private ServerConfigurationProperties props;
    private Pattern passwordPattern;
    private String message;

    @Autowired
    public void setProperties(final ServerConfigurationProperties props) {
        this.props = props;
    }

    @Override
    public void initialize(PasswordPattern constraintAnnotation) {
        final String passwordRegex = "^.+$";
        passwordPattern = Pattern.compile(passwordRegex);

        message = String.format(constraintAnnotation.message(), props.getMinPasswordLength());
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.disableDefaultConstraintViolation();
        constraintValidatorContext
                .buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();

        return password != null
                && passwordPattern.matcher(password).matches()
                && password.length() >= props.getMinPasswordLength();
    }
}
