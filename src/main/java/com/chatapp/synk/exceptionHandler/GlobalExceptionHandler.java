package com.chatapp.synk.exceptionHandler;

import com.chatapp.synk.response.BeanValidationErrors;
import com.chatapp.synk.response.ConstraintValidationErrors;
import com.chatapp.synk.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ControllerAdvice
public class GlobalExceptionHandler {

    private final int ERROR_CODE_BADREQUEST = 400;
    private final int ERROR_CODE_INTERNALSERVERERROR = 500;

    @ExceptionHandler({ServiceException.class})
    public ResponseEntity<ErrorResponse> handleServiceException(ServiceException exception) {
        ErrorResponse errResp = new ErrorResponse();
        errResp.setResponseCode(ERROR_CODE_INTERNALSERVERERROR);
        errResp.setError(HttpStatus.INTERNAL_SERVER_ERROR);
        errResp.setErrorMessage(exception.getMessage());
        return new ResponseEntity<ErrorResponse>(errResp, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> processFieldValidationException(final MethodArgumentNotValidException ex) {

        List<BeanValidationErrors> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(i -> new BeanValidationErrors(i.getField(), i.getDefaultMessage(), i.getRejectedValue()))
                .collect(Collectors.toList());

        ErrorResponse<BeanValidationErrors> resp = new ErrorResponse();
        resp.setResponseCode(ERROR_CODE_BADREQUEST);
        resp.setError(HttpStatus.BAD_REQUEST);
        resp.setErrors(errors);
        return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
    }

//    @ExceptionHandler(ConstraintViolationException.class)
//    ResponseEntity<ErrorResponse> onConstraintValidationException(ConstraintViolationException e) {
//
//        List<ConstraintValidationErrors> errors = new ArrayList<>();
//        for (ConstraintViolation violation : e.getConstraintViolations()) {
//            errors.add(new ConstraintValidationErrors(violation.getPropertyPath().toString(), violation.getMessage()));
//        }
//        ErrorResponse<ConstraintValidationErrors> resp = new ErrorResponse();
//        resp.setResponseCode(ERROR_CODE_BADREQUEST);
//        resp.setError(HttpStatus.BAD_REQUEST);
//        resp.setErrors(errors);
//        return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
//    }

}
