package net.thumbtack.forums.validator.user;

import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = UsernamePatternValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UsernamePattern {
    String message() default "username must contain only latin or cyrillic letters or digits and shorter than %s symbols";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
