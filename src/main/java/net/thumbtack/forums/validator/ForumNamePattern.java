package net.thumbtack.forums.validator;

import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = ForumNamePatternValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ForumNamePattern {
    String message() default "forum name must contain only latin or cyrillic letters and shorter than %s symbols";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
