package net.thumbtack.forums.validator.message;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class AvailableRatingValidator implements ConstraintValidator<AvailableRating, Integer> {
    @Override
    public boolean isValid(Integer rating, ConstraintValidatorContext constraintValidatorContext) {
        if (rating == null) {
            return true;
        }
        return rating >= 1 && rating <= 5;
    }
}
