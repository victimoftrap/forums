package net.thumbtack.forums.validator.forum;

import net.thumbtack.forums.configuration.ServerConfigurationProperties;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ForumNamePatternValidator implements ConstraintValidator<ForumNamePattern, String> {
    private ServerConfigurationProperties props;
    private Pattern forumNamePattern;
    private String message;

    @Autowired
    public void setProps(final ServerConfigurationProperties props) {
        this.props = props;
    }

    @Override
    public void initialize(ForumNamePattern constraintAnnotation) {
        final String usernameRegex = "^[\\p{Alpha}\\p{IsCyrillic} ]+$";
        forumNamePattern = Pattern.compile(usernameRegex);
        message = String.format(constraintAnnotation.message(), props.getMaxNameLength());
    }

    @Override
    public boolean isValid(String forumName, ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.disableDefaultConstraintViolation();
        constraintValidatorContext
                .buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
        return forumName != null
                && forumNamePattern.matcher(forumName).matches()
                && forumName.length() < props.getMaxNameLength();
    }
}
