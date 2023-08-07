package mooncake.example.bank.handler.aop;

import mooncake.example.bank.handler.exception.CustomValidationException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

/*
 AOP 를 통해 Controller 의 역할 분리
 */

@Component
@Aspect
public class CustomValidationAdvice {
    /*
     Body 데이터가 있을 시 사용되는 Validation AOP
     - 두 개의 Method 에 대해 Point Cut 을 잡는다
     */

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postMapping() {
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public void putMapping() {
    }

    // JoinPoint, 전후 제어 // @After, @Before 는 각각 하나
    @Around("postMapping() || putMapping()")
    public Object catchValidationError(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs();// 해당 AOP 대상 메소드로 진입하는 PARAMS 들을 받아온다
        for (Object arg : args) {
            if (arg instanceof BindingResult) { // 들어오는 Validation 의 결과 중 Error 가 있는지 확인한다
                BindingResult br = (BindingResult) arg;

                Map<String, String> errMap = new HashMap<>(); // 필드별로 발생한 에러 (BR 의 이유) 를 Exception 으로 전달하여, 바디로 전달될 수 있도록 세팅해준다
                for (FieldError fe : br.getFieldErrors()) {
                    errMap.put(fe.getField(), fe.getDefaultMessage());
                }

                throw new CustomValidationException("[유효성 검사 실패] 들어오는 PARAMS 들을 확인하세요", errMap);
            }
        }

        return proceedingJoinPoint.proceed();
    }
}
