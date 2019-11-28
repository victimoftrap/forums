package net.thumbtack.forums.dto.exception;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class ExceptionListDtoResponse {
    private List<ExceptionDtoResponse> errors;

    public ExceptionListDtoResponse() {
        errors = new ArrayList<>();
    }

    public void addError(final ExceptionDtoResponse exception) {
        errors.add(exception);
    }

    public List<ExceptionDtoResponse> getErrors() {
        errors.sort(Comparator.comparing(ExceptionDtoResponse::getField));
        return Collections.unmodifiableList(errors);
    }
}
