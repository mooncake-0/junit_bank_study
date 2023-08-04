package mooncake.example.bank.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import mooncake.example.bank.dto.ResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;

/*
 Status Type 별로 에러 상태 제어가 필요하다
 - 내가 모르는 에러가 API 결과로 나오지 않도록 하는 것이 중요한 목표
 */
public class CustomResponseUtils {

    private static final Logger log = LoggerFactory.getLogger(CustomResponseUtils.class); //static 이므로 getClass() 사용 불가

    /*
     403 과 401 은 다릅니다
     401 - UnAuthorized - 권한을 확인할 수 있는 정보가 없음, 부족함 등등 정확한 인식이 불가한 상황
     403 - Forbidden - 이 사람의 권한은 명확히 확인이 되었지만, 진입하려는 곳으로 진입하기엔 권한이 부족하다
     */
    public static void errResponse(HttpServletResponse response, String errMsg, HttpStatus statusCode) {

        /*
         401 - 미인증 및 권한 확인 어려움
         403 - 권한 부족
         404 - 해당 경로에 대한 백엔드 서버가 대응하지 않음
         */

        try {
            ObjectMapper om = new ObjectMapper();
            ResponseDto<?> responseDto = new ResponseDto<>(-1, errMsg, null);
            String responseBody = om.writeValueAsString(responseDto);

            response.setContentType("application/json; charset=utf-8"); // JSON 으로 줄것임
            response.setStatus(statusCode.value());
            response.getWriter().println(responseBody);

        } catch (Exception e) {
            log.error("서버 파싱 에러");
        }

    }
}


