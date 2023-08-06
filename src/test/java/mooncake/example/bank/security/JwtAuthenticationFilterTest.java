package mooncake.example.bank.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import mooncake.example.bank.config.dummy.DummyObjectCreator;
import mooncake.example.bank.config.security.jwt.JwtParams;
import mooncake.example.bank.domain.user.User;
import mooncake.example.bank.domain.user.UserEnum;
import mooncake.example.bank.domain.user.UserRepository;
import mooncake.example.bank.dto.request.UserReqDto;
import mooncake.example.bank.dto.response.UserRespDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.transaction.TransactionScoped;
import javax.transaction.Transactional;

import static mooncake.example.bank.dto.request.UserReqDto.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles(value = "test")
@Transactional
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class JwtAuthenticationFilterTest extends DummyObjectCreator {


    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository; //가짜 환겨이여도 주입은 되나봄. SpringBootTest 는 컨테이너 주입은 다 하는듯

    @Autowired
    private ObjectMapper om;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    /*
     Test 마다 주입 선택 - Test 에 Tx 지정해서 BE 의 DB 를 Test 마다 완전히 독립적으로 가져갈 수 있게 해줘야한다
     - 그래야 연속 실행 가능. test 환경에서 @Tx 의 기본 세팅은 Rollback
     */
    @BeforeEach
    void be() {

        // TEst 용 객체 생성자도 만들어 놓으면 매우 편할듯 깔끔하고
        User user = User.builder()
                .username("mooncake")
                .password(passwordEncoder.encode("1234"))
                .fullName("문케이크")
                .email("email@email.com")
                .role(UserEnum.CUSTOMER)
                .build();

        userRepository.save(user);
    }

    @Test
    void successful_when_sending_right_Login() throws Exception {

        //given
        LoginReqDto loginReqDto = newMockLoginDto("mooncake", "1234");
        String requestBody = om.writeValueAsString(loginReqDto);

        //when
        ResultActions resultActions = mvc.perform(post("/api/login").content(requestBody).contentType(MediaType.APPLICATION_JSON));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        String receivedToken = resultActions.andReturn().getResponse().getHeader(JwtParams.HEADER);

        //then
        System.out.println(responseBody);
        resultActions.andExpect(status().isOk());
        assertNotNull(receivedToken);
        assertThat(receivedToken.startsWith(JwtParams.PREFIX)).isTrue();
        resultActions.andExpect(jsonPath("$.username").value("mooncake")); // json 의 값도 확인해볼 수 있다
    }

    @Test
    public void unsuccessfulAuthentication_test() throws Exception {

        //given > DTO 필요, 역할체들 잘 생성되는지 Test
        LoginReqDto loginReqDto = new LoginReqDto();
        loginReqDto.setUsername("mooncake");
        loginReqDto.setPassword("12345");
        String requestBody = om.writeValueAsString(loginReqDto);

        //when
        ResultActions perform = mvc.perform(post("/api/login").content(requestBody).contentType(MediaType.APPLICATION_JSON));
        String responseBody = perform.andReturn().getResponse().getContentAsString();
        String jwtToken = perform.andReturn().getResponse().getHeader(JwtParams.HEADER);

        //then
        perform.andExpect(status().isUnauthorized());
    }
}
