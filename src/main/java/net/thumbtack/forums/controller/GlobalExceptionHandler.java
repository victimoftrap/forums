package net.thumbtack.forums.controller;

import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.dto.exception.ExceptionDtoResponse;
import net.thumbtack.forums.dto.exception.ExceptionListDtoResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ServerException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionListDtoResponse handleError(final ServerException ex) {
        final ExceptionListDtoResponse exceptionResponse = new ExceptionListDtoResponse();
        exceptionResponse.addError(
                new ExceptionDtoResponse(
                        ex.getErrorCode(),
                        ex.getErrorField().getName(),
                        ex.getMessage()
                )
        );
        return exceptionResponse;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionListDtoResponse handleValidationException(final MethodArgumentNotValidException exc) {
        final ExceptionListDtoResponse exceptionResponse = new ExceptionListDtoResponse();
        exc.getBindingResult()
                .getFieldErrors()
                .forEach(fieldError -> exceptionResponse.addError(
                        new ExceptionDtoResponse(
                                ErrorCode.INVALID_REQUEST_DATA,
                                fieldError.getField(),
                                fieldError.getDefaultMessage()
                        ))
                );
        return exceptionResponse;
    }
}
