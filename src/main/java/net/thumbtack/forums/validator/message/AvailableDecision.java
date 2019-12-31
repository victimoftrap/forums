package net.thumbtack.forums.validator.message;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AvailableDecisionValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AvailableDecision {
    String message() default "decision must be not blank and one of %s";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
