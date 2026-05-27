package com.customerservice.exception;

import com.customerservice.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String USERNAME_ALREADY_EXISTS = "USERNAME_ALREADY_EXISTS";

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
        ErrorResponse body = new ErrorResponse();
        body.setCode(USERNAME_ALREADY_EXISTS);
        body.setMessage("Tên đăng nhập đã tồn tại.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ignored) {
        ErrorResponse body = new ErrorResponse();
        body.setCode(INVALID_CREDENTIALS);
        body.setMessage("Tên đăng nhập hoặc mật khẩu không hợp lệ.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }
}
