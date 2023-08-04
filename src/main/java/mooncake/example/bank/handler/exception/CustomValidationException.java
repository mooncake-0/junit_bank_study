package mooncake.example.bank.handler.exception;

import lombok.Getter;
import mooncake.example.bank.handler.aop.CustomValidationAdvice;

import java.util.Map;

@Getter
public class CustomValidationException extends RuntimeException{

    private final Map<String, String> errMap;

    public CustomValidationException(String errMsg, Map<String, String> errMap) {
        super(errMsg);
        this.errMap = errMap;
    }
}
