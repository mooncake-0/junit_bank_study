package mooncake.example.bank.handler;

import mooncake.example.bank.dto.ResponseDto;
import mooncake.example.bank.handler.aop.CustomValidationAdvice;
import mooncake.example.bank.handler.exception.CustomApiException;
import mooncake.example.bank.handler.exception.CustomValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // Exception Handling 공간
public class CustomExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(CustomApiException.class)
    public ResponseEntity<ResponseDto<Object>> customApiException(CustomApiException exception) {

        log.error("CUSTOM API EXCEPTION : {}", exception.getMessage());
        return new ResponseEntity<>(new ResponseDto<>(-1, exception.getMessage(), null), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CustomValidationException.class)
    public ResponseEntity<ResponseDto<Object>> customValidationException(CustomValidationException exception) {
        log.error("CUSTOM VALIDATION EXCEPTION : {}", exception.getMessage());
        return new ResponseEntity<>(new ResponseDto<>(-1, exception.getMessage(), exception.getErrMap()), HttpStatus.BAD_REQUEST);
    }

}
