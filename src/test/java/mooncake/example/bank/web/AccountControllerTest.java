package mooncake.example.bank.web;


import com.fasterxml.jackson.databind.ObjectMapper;
import mooncake.example.bank.config.dummy.DummyObjectCreator;
import mooncake.example.bank.config.security.auth.LoginUser;
import mooncake.example.bank.config.security.jwt.JwtParams;
import mooncake.example.bank.config.security.jwt.JwtProcessor;
import mooncake.example.bank.domain.account.Account;
import mooncake.example.bank.domain.account.AccountRepository;
import mooncake.example.bank.domain.user.User;
import mooncake.example.bank.domain.user.UserEnum;
import mooncake.example.bank.domain.user.UserRepository;
import mooncake.example.bank.dto.request.AccountReqDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static mooncake.example.bank.dto.request.AccountReqDto.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@Sql("classpath:db/teardown.sql") // 설명 해당 파일에 적혀있기 한데 필수적일 필요는 없을 듯 // 막 그렇게 땡기는 설정은 아닌듯
@AutoConfigureMockMvc
@ActiveProfiles(value = "test")
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class AccountControllerTest extends DummyObjectCreator {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;
    // MEMO :: 실제 요청 환경과 동일하게 Test 를 해주기 위해서, 영속화 컨텍스트를 비워주는게 좋다
    @Autowired
    private EntityManager em;
    @BeforeEach
    void be() {

        /*
         TEST 시에만 객체를 편하게 만들 수 있도록 하는 Method 를 만들어주면 좋을듯 - Test PACKAGE 에서만 사용 가능하게? protected 같은걸 설정하면서 만들어보자
         - TestObjectCreator Class 를 상속받는 공간을 무조건 TEST PCKG 내로만!! 할 수는 없나?
         */
        user = User.builder()
                .username("mooncake")
                .password(passwordEncoder.encode("1234"))
                .email("mooncake@mc.com")
                .fullName("문케이크")
                .role(UserEnum.CUSTOMER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userRepository.save(user); // DATA JPA 는 Repos 안에 @Tx 걸려있어서 바로바로 쿼리 나감

        User another = User.builder()
                .username("another")
                .password(passwordEncoder.encode("1234"))
                .email("another@mc.com")
                .fullName("another")
                .role(UserEnum.CUSTOMER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userRepository.save(another);

        Account mooncakeAccount = Account.builder()
                .number(1111L)
                .password(1234L)
                .balance(1000L)
                .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        accountRepository.save(mooncakeAccount);

        // QUERY 나가는 모습을 정확히 확인하기 위함 (LAZY LOADING JOIN 같은거)
        em.clear();

    }

    /*
     Mocking 환경에서 요청자를 담을 수 있는 방법
     - DB (Test 단) 에서 조회를 하여, 이 사람이 요청하고 있다를 넣어주는 annotation - @WithUserDetails
     - 물론 BeforeEach 로 mooncake 을 넣어줘야함
     - setupBefore=TEST_METHOD // 이건 SU 메서드 전에 (Before Each) 수행된다 - 그니까 이 default 설정으로 하면 user 가 저장 안된 상태에서 불러오려하기 때문에 바꿔줘야 함
     - setupBefore=TEST_EXECUTION // 테스트 대상 메서드 직전에 수행된다
     */
    /*
     JWT 난릴 필요 없는 이유
     > account 가 실현될때 Security 에 Session 만 있으면 되는 것 // 이걸 하는 부분이 Contoller 인게 아니라, Security 부분에서 최종적으로 확인한다
     > 그리고 Authentication 객체 없으면 당연히 Spring 단에 넘어가기 전에 에러를 터뜨린다
     > 하지만 @WithUserDetails 는 요청시 mooncake 의 Authentication 객체를 넣은 상태로 요청을 날리는 것
     > 따라서 Security 에러를 통과한다
     > 그러므로 JWT TOKEN 필요 없음
     > 참고로 AuthorizationFilter 에서 if 문 밖에 doFilter 를 뒀기 때문임. 토큰 없다고 에러 때리지 않은 이유임
     */
    @WithUserDetails(setupBefore = TestExecutionEvent.TEST_EXECUTION, value = "another")
    @Test
    // 해당 username 을 사용해서 찾아온다 // 이렇게 하면
    void account_should_be_saved_with_right_info_and_token() throws Exception {

        //given (USER 필요)
        AccountSaveReqDto requestDto = newMockAccountSaveReqDto(1000L, 1234L);
        String requestBody = om.writeValueAsString(requestDto);

        //given - 토큰 생성 (해당 유저가 회원가입 되어 있고, 토큰을 전달해준 상태임을 가정) (위에서 @WithUserDetails 로 Authentiation 객체 넣어줌)
//        User mockUser = newMockUser(1L, "mooncake", "문케이크");
//        LoginUser mockLoginUser = new LoginUser(mockUser);
//        String jwtTokenWithHeader = JwtProcessor.create(mockLoginUser);

        //when
        ResultActions resultActions = mvc.perform(post("/api/s/account")
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));

        //then
        resultActions.andExpect(status().isCreated());
    }

    @Test
    @WithUserDetails(setupBefore = TestExecutionEvent.TEST_EXECUTION, value = "mooncake")
    void when_right_info_passed_should_delete_account() throws Exception {

        System.out.println("A DEBUGGING " +  user.getId());

        // given
        Long accountNumber = 1111L;

        // when
        ResultActions resultActions = mvc.perform(delete("/api/s/account/" + accountNumber));
        /*
         참고로 DELETE QUERY 는 원래 안됨.
         */
        // then
        resultActions.andExpect(status().isOk());

        // 추가 검증 -> 조회되는지
        Optional<Account> accountOp = accountRepository.findByNumber(accountNumber);
        assertTrue(accountOp.isEmpty());

    }


    @Test
    @WithUserDetails(setupBefore = TestExecutionEvent.TEST_EXECUTION, value = "another")
    void when_another_user_requests_mooncake_delete_should_throw_error() throws Exception {

        // given
        Long accountNumber = 1111L;

        // when
        ResultActions resultActions = mvc.perform(delete("/api/s/account/" + accountNumber));

        // then
        resultActions.andExpect(status().isBadRequest());


    }
}
