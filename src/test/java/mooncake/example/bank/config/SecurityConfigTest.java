package mooncake.example.bank.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.assertj.core.api.Assertions.*;

@ActiveProfiles(value = "test")
@AutoConfigureMockMvc // 가짜 환경을 위한 MockMvc 를 Bean 등록처리 해준다
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc; // Mock 환경  컨테이너에서 @AutoConfigure 을 통해 주입된 MockMvc 를 주입해준다

    /*
     사이트 들어가거나, API Tester 등을 통해서 했던 것을
     이렇게 JUNIT 통합 테스트로 진행할 수 있다
     */

    // 일괄적인 Data 전달을 위해 이 단계에서 ResponseDto 만들고
    // Utils 사용하여 제어하기 시작!
    // 왜냐하면 어떤 Tool 로 요청하냐에 따라서 너무 달랐음
    // ResponseDto 로 에러 메시지를 제어해주니까 내가 만든 Error 형태로 일괄되게 전달할 수 있다
    @Test
    @DisplayName("인증이 안된 상태로 API 접근 시도시 401 에러")
    void authentiaction_test() throws Exception {

        //given
        //when
        ResultActions resultActions = mockMvc.perform(get("/api/s/anything"));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString(); // 이런식으로 받는다
        int statusCode = resultActions.andReturn().getResponse().getStatus();

        //then
        assertThat(statusCode).isEqualTo(401);
    }

    @Test
    @DisplayName("인증이 안된 상태로 ADMIN API 접근 시도해도 401 에러 (인가 확인도 하기 전에 에러)")
    void authorization_test() throws Exception {

        //given
        //when
        ResultActions resultActions = mockMvc.perform(get("/api/admin/anything"));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        int statusCode = resultActions.andReturn().getResponse().getStatus();

        //then
        assertThat(statusCode).isEqualTo(401);
    }
}
