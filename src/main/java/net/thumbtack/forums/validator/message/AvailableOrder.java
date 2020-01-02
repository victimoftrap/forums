package net.thumbtack.forums.validator.message;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AvailableOrderValidator.class)
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AvailableOrder {
    String message() default "order must be one of %s";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
