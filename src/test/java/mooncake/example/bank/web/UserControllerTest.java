package mooncake.example.bank.web;


import com.fasterxml.jackson.databind.ObjectMapper;
import mooncake.example.bank.config.dummy.DummyObjectCreator;
import mooncake.example.bank.domain.user.UserRepository;
import mooncake.example.bank.dto.request.UserReqDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class UserControllerTest extends DummyObjectCreator {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;


    @Test
    @DisplayName("성공 @ join()")
    void join_test1() throws Exception {
        //given
        UserReqDto.UserJoinReqDto requestDto = newMockRequestDto("mooncake", "12345", "mooncake@mc.com", "문케이크");
        String requestBody = om.writeValueAsString(requestDto);

        //when
        ResultActions rs = mvc.perform(post("/api/join").content(requestBody).contentType(MediaType.APPLICATION_JSON));

        //then
        rs.andExpect(status().isCreated());
    }

    /*
     */
    @Test
    @DisplayName("실패 @ join() _ 중복되는 username")
    void join_test2() throws Exception {

        // given (미리 저장해두자)
        // 참고로 Data JPA 에는 자동으로 Repository method 마다 @Tx 가 붙는걸로 확인된다. --> 호출되자마자 Tx 종료로 sql 이 발생해서 저장이 된다
        // 그리고 연이은 Test 를 위해 Tx 를 이 Test 에도 달아줘야 한다. 안그러면 위에 저장한거로 인해 given 에서 에러 발생한다
        userRepository.save(newMockUser(100L, "mooncake", "문케이크"));
        // 그래서 롤백을 해줘야함

        UserReqDto.UserJoinReqDto requestDto = newMockRequestDto("mooncake", "12345", "mooncake@mc.com", "문케이크");
        String requestBody = om.writeValueAsString(requestDto);

        //when
        ResultActions rs = mvc.perform(post("/api/join").content(requestBody).contentType(MediaType.APPLICATION_JSON));

        //then
        rs.andExpect(status().isBadRequest());

    }

}
