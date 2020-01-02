package net.thumbtack.forums.validator.user;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordPatternValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordPattern {
    String message() default "password must be not blank and longer than %s symbols";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
