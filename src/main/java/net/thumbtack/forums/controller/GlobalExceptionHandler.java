package net.thumbtack.forums.controller;

import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.dto.responses.exception.ExceptionDtoResponse;
import net.thumbtack.forums.dto.responses.exception.ExceptionListDtoResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolationException;

import static net.thumbtack.forums.exception.ErrorCode.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ServerException.class)
    public ResponseEntity<ExceptionListDtoResponse> handleServerException(final ServerException ex) {
        final ExceptionListDtoResponse exceptionResponse = new ExceptionListDtoResponse();
        exceptionResponse.addError(
                new ExceptionDtoResponse(
                        ex.getErrorCode().name(),
                        ex.getErrorCode().getErrorCauseField(),
                        ex.getErrorCode().getMessage()
                )
        );

        final ErrorCode errorCode = ex.getErrorCode();
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        if (errorCode == USER_NOT_FOUND || errorCode == FORUM_NOT_FOUND || errorCode == MESSAGE_NOT_FOUND) {
            httpStatus = HttpStatus.NOT_FOUND;
        }
        return ResponseEntity
                .status(httpStatus)
                .body(exceptionResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionListDtoResponse handleValidationException(final MethodArgumentNotValidException exc) {
        final ExceptionListDtoResponse exceptionResponse = new ExceptionListDtoResponse();
        exc.getBindingResult()
                .getFieldErrors()
                .forEach(fieldError -> exceptionResponse.addError(
                        new ExceptionDtoResponse(
                                ErrorCode.INVALID_REQUEST_DATA.name(),
                                fieldError.getField(),
                                fieldError.getDefaultMessage()
                        ))
                );
        return exceptionResponse;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionListDtoResponse handleMethodParameterException(final ConstraintViolationException cve) {
        final ExceptionListDtoResponse exceptionResponse = new ExceptionListDtoResponse();

        cve.getConstraintViolations()
                .forEach(error -> exceptionResponse.addError(
                        new ExceptionDtoResponse(
                                ErrorCode.INVALID_REQUEST_DATA.name(),
                                error.getPropertyPath().toString().split("\\.")[1],
                                error.getMessage()
                        ))
                );
        return exceptionResponse;
    }
}
