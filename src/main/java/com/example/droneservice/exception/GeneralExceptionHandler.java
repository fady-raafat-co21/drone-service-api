package com.example.droneservice.exception;


import jakarta.persistence.EntityExistsException;
import jakarta.validation.ConstraintViolationException;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.webjars.NotFoundException;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GeneralExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String ERROR_MESSAGE_TEMPLATE = "message: %s %n requested uri: %s";
    private static final String LIST_JOIN_DELIMITER = ",";
    private static final String FIELD_ERROR_SEPARATOR = ": ";
    private static final Logger local_logger = LoggerFactory.getLogger(GeneralExceptionHandler.class);
    private static final String ERRORS_FOR_PATH = "errors {} for path {}";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatErrorMessage)
                .collect(Collectors.toList());
        return getExceptionResponseEntity(ex, HttpStatus.BAD_REQUEST, request, validationErrors);
    }
    private String formatErrorMessage(FieldError error) {
        return error.getField() + FIELD_ERROR_SEPARATOR + error.getDefaultMessage();
    }


    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException exception, WebRequest request) {
        final List<String> validationErrors = exception.getConstraintViolations().stream().
                map(violation ->
                        violation.getPropertyPath() + FIELD_ERROR_SEPARATOR + violation.getMessage())
                .collect(Collectors.toList());
        return getExceptionResponseEntity(exception, HttpStatus.BAD_REQUEST, request, validationErrors);
    }
    @ExceptionHandler({EntityExistsException.class})
    public ResponseEntity<Object> handleEntityExistsException(Exception exception, WebRequest request) {
        final String path = request.getDescription(false);
        logger.error(String.format(ERROR_MESSAGE_TEMPLATE, exception.getMessage(), path), exception);
        return getExceptionResponseEntity(exception, HttpStatus.CONFLICT, request, Collections.singletonList(exception.getMessage()));
    }
    @ExceptionHandler({NotAcceptException.class})
    public ResponseEntity<Object> handleNotAcceptException(Exception exception, WebRequest request) {
        final String path = request.getDescription(false);
        logger.error(String.format(ERROR_MESSAGE_TEMPLATE, exception.getMessage(), path), exception);
        return getExceptionResponseEntity(exception, HttpStatus.NOT_ACCEPTABLE, request, Collections.singletonList(exception.getMessage()));
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<Object> handleNotFoundException(Exception exception, WebRequest request) {
        final String path = request.getDescription(false);
        logger.error(String.format(ERROR_MESSAGE_TEMPLATE, exception.getMessage(), path), exception);
        return getExceptionResponseEntity(exception, HttpStatus.NOT_FOUND, request, Collections.singletonList(exception.getMessage()));
    }

    /**
     * A general handler for all uncaught exceptions
     */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAllExceptions(Exception exception, WebRequest request) {
        ResponseStatus responseStatus =
                exception.getClass().getAnnotation(ResponseStatus.class);
        final HttpStatus status =
                responseStatus!=null ? responseStatus.value():HttpStatus.INTERNAL_SERVER_ERROR;
        final String localizedMessage = exception.getLocalizedMessage();
        final String path = request.getDescription(false);
        String message = (StringUtils.isNotEmpty(localizedMessage) ? localizedMessage:status.getReasonPhrase());
        logger.error(String.format(ERROR_MESSAGE_TEMPLATE, message, path), exception);
        return getExceptionResponseEntity(exception, status, request, Collections.singletonList(message));
    }

    /**
     * Build a detail information about the exception in the response
     */
    private ResponseEntity<Object> getExceptionResponseEntity(final Exception exception,
                                                              final HttpStatus status,
                                                              final WebRequest request,
                                                              final List<String> errors) {
        final String path = request.getDescription(false);
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .instant(Instant.now())
                .status(status.value())
                .errors(CollectionUtils.isEmpty(errors) ? Collections.singletonList(status.getReasonPhrase()) : errors)
                .type(exception.getClass().getSimpleName())
                .path(path)
                .message(status.getReasonPhrase())
                .build();
        final String errorsMessage = CollectionUtils.isEmpty(errors) ? status.getReasonPhrase()
                : errors.stream().filter(StringUtils::isNotEmpty).collect(Collectors.joining(LIST_JOIN_DELIMITER));
        local_logger.error(ERRORS_FOR_PATH, errorsMessage, path);
        return new ResponseEntity<>(exceptionResponse, status);
    }
}
