package net.thumbtack.forums.validator.message;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AvailableMessagePriorityValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AvailableMessagePriority {
    String message() default "priority must be one of %s";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
