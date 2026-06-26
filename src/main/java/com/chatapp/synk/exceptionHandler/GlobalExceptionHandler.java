package com.chatapp.synk.exceptionHandler;

import com.chatapp.synk.response.BeanValidationErrors;
import com.chatapp.synk.response.ConstraintValidationErrors;
import com.chatapp.synk.response.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@ControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(ServiceException.class)
        public ResponseEntity<ErrorResponse<Void>> handleServiceException(ServiceException exception) {
                logger.warn("ServiceException occurred: {} stack trace: {}", exception.getMessage(), exception);
                HttpStatus status = exception.getStatus();
                return ResponseEntity.status(status)
                                .body(new ErrorResponse<Void>(
                                                status.value(),
                                                status,
                                                exception.getMessage()));
        }

        @ExceptionHandler(InvalidTokenException.class)
        public ResponseEntity<ErrorResponse<Void>> handleInvalidToken(InvalidTokenException ex) {
                logger.warn("InvalidTokenException occured: {} stack trace: {}", ex.getMessage(), ex);

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(new ErrorResponse<Void>(
                                                HttpStatus.UNAUTHORIZED.value(),
                                                HttpStatus.UNAUTHORIZED,
                                                ex.getMessage()));
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
                logger.warn("AccessDeniedException occured: {} stack trace: {}", ex.getMessage(), ex);

                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(new ErrorResponse<Void>(
                                                HttpStatus.FORBIDDEN.value(),
                                                HttpStatus.FORBIDDEN,
                                                ex.getMessage()));
        }

        // This method calls as method has @transactional annotation 
        // on repo.save DataIntegrityViolationException throws 
        // from outside try catch block so we need to handle 
        // it here in global exception handler
        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ErrorResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
                // Log the full raw SQL error for debugging — never send it to the client
                logger.warn("DataIntegrityViolationException: {}", ex.getMessage());

                String friendlyMessage = resolveDuplicateEntryMessage(ex);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(new ErrorResponse<Void>(
                                                HttpStatus.CONFLICT.value(),
                                                HttpStatus.CONFLICT,
                                                friendlyMessage));
        }

        private String resolveDuplicateEntryMessage(DataIntegrityViolationException ex) {
                String rootMsg = ex.getMostSpecificCause().getMessage();
                if (rootMsg != null && rootMsg.contains("Duplicate entry")) {
                        // MySQL format: Duplicate entry '<value>' for key '<constraint>'
                        Matcher matcher = Pattern.compile("Duplicate entry '(.+?)' for key").matcher(rootMsg);
                        if (matcher.find()) {
                                String duplicateValue = matcher.group(1);
                                if (duplicateValue.contains("@")) {
                                        return "An account with this email address already exists";
                                } else {
                                        return "An account with this phone number already exists";
                                }
                        }
                        return "An account with this information already exists";
                }
                return "A conflict occurred. Please check your input and try again";
        }

        @ExceptionHandler(Exception.class) // catches Runtime Exception as well
        public ResponseEntity<ErrorResponse<Void>> handleOtherExceptions(Exception ex) {
                logger.error("Exception occured: {} stack trace: {}", ex.getMessage(), ex);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new ErrorResponse<Void>(
                                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                HttpStatus.INTERNAL_SERVER_ERROR,
                                                ex.getMessage()));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse<BeanValidationErrors>> processFieldValidationException(
                        final MethodArgumentNotValidException ex) {
                logger.warn("Validation failed: {} field(s)", ex.getBindingResult().getFieldErrorCount());

                List<BeanValidationErrors> errors = ex.getBindingResult().getFieldErrors().stream()
                                .map(i -> new BeanValidationErrors(i.getField(), i.getDefaultMessage(),
                                                i.getRejectedValue()))
                                .collect(Collectors.toList());

                ErrorResponse<BeanValidationErrors> resp = new ErrorResponse<BeanValidationErrors>();
                resp.setResponseCode(HttpStatus.BAD_REQUEST.value());
                resp.setError(HttpStatus.BAD_REQUEST);
                resp.setErrors(errors);

                return new ResponseEntity<ErrorResponse<BeanValidationErrors>>(resp, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ErrorResponse<ConstraintValidationErrors>> onConstraintValidationException(
                        ConstraintViolationException e) {
                logger.warn("Constraint violations detected: {}", e.getConstraintViolations().size());

                List<ConstraintValidationErrors> errors = new ArrayList<>();
                for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
                        errors.add(new ConstraintValidationErrors(violation.getPropertyPath().toString(),
                                        violation.getMessage()));
                }

                ErrorResponse<ConstraintValidationErrors> resp = new ErrorResponse<ConstraintValidationErrors>();
                resp.setResponseCode(HttpStatus.BAD_REQUEST.value());
                resp.setError(HttpStatus.BAD_REQUEST);
                resp.setErrors(errors);

                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
}
