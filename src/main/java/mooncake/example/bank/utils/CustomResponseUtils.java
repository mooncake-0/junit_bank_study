package mooncake.example.bank.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import mooncake.example.bank.dto.ResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

/*
 Status Type 별로 에러 상태 제어가 필요하다
 - 내가 모르는 에러가 API 결과로 나오지 않도록 하는 것이 중요한 목표
 */
public class CustomResponseUtils {

    private static final Logger log = LoggerFactory.getLogger(CustomResponseUtils.class); //static 이므로 getClass() 사용 불가


    /*
     미인증 에러 - 401
     */
    public static void unAuthenticatedResponse(HttpServletResponse response, String errorMsg) {

        try {

            ObjectMapper om = new ObjectMapper();
            ResponseDto<?> responseDto = new ResponseDto<>(-1, errorMsg, null);
            String responseBody = om.writeValueAsString(responseDto);

            response.setContentType("application/json; charset=utf-8"); // JSON 으로 줄것임
            response.setStatus(401);
            response.getWriter().println(responseBody);

        } catch (Exception e) {
            log.error("서버 파싱 에러");
        }

    }
}
