package net.thumbtack.forums.validator.user;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EmailPatternValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EmailPattern {
    String message() default "email not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
