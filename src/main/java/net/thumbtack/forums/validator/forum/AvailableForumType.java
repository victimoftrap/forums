package net.thumbtack.forums.validator.forum;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AvailableForumTypeValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AvailableForumType {
    String message() default "forum type must be not blank and one of %s";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
