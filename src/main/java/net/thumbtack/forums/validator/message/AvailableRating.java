package net.thumbtack.forums.validator.message;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AvailableRatingValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AvailableRating {
    String message() default "rating must be from 1 to 5";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
