package mooncake.example.bank.security;


import mooncake.example.bank.config.dummy.DummyObjectCreator;
import mooncake.example.bank.config.security.auth.LoginUser;
import mooncake.example.bank.config.security.jwt.JwtParams;
import mooncake.example.bank.config.security.jwt.JwtProcessor;
import mooncake.example.bank.domain.user.User;
import mooncake.example.bank.domain.user.UserEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class JwtAuthorizationFilterTest extends DummyObjectCreator {

    @Autowired
    private MockMvc mvc;
    private User user;
    private LoginUser loginUser;

    @BeforeEach
    void given_be() {
        user = newMockUser(1L, "mooncake", "문케이크");
        loginUser = new LoginUser(user);
    }

    @Test
    @DisplayName("토큰을 헤더에 걸고 API 를 요청하면 인증은 통과한다")
    public void authorization_success_test() throws Exception {

        //given
        String token = JwtProcessor.create(loginUser); // 토큰을 헤더에 걸고 API 를 요청하려고 한다

        //when (헤더에 token 추가)
        ResultActions perform = mvc.perform(get("/api/s/hello/test").header(JwtParams.HEADER, token));// 404 예상

        //then
        perform.andExpect(status().isNotFound()); // 인증을 통과했으니 404 로 간다.
    }

    @Test
    @DisplayName("토큰을 헤더에 걸지 않고 API 요청하면 (유효한 인증 정보 부족) 401 UnAuthorized 이다 - 현재 유저의 권한 정보가 없다..")
    void authorization_fail_test() throws Exception {

        //given // 아무 인증정보 없이 진입하려 한다
        //when
        ResultActions perform = mvc.perform(get("/api/s/hello/test"));

        //then
        perform.andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("토큰을 헤더에 걸었고, 권한정보도 명확히 확인. but 판단한 결과 유저의 권한이 부족하다면 403 Forbidden 이다")
    void authorization_fail_by_forbidden_test() throws Exception {

        //given
        user.setRole(UserEnum.CUSTOMER);
        loginUser = new LoginUser(user);
        String token = JwtProcessor.create(loginUser);

        //when
        ResultActions perform = mvc.perform(get("/api/admin/hello").header(JwtParams.HEADER, token));

        //then
        perform.andExpect(status().isForbidden());
    }

}