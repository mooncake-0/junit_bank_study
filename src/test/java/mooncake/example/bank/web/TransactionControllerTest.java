package mooncake.example.bank.web;

import mooncake.example.bank.config.dummy.TestObjectCreator;
import mooncake.example.bank.domain.account.Account;
import mooncake.example.bank.domain.account.AccountRepository;
import mooncake.example.bank.domain.transaction.Transaction;
import mooncake.example.bank.domain.transaction.TransactionRepository;
import mooncake.example.bank.domain.user.User;
import mooncake.example.bank.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Transactional
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class TransactionControllerTest extends TestObjectCreator{

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void be() {

        autoIncrementReset();
        dataSu();
        em.clear();
    }

    private void autoIncrementReset() {
        em.createNativeQuery("ALTER TABLE user_tb ALTER COLUMN id RESTART WITH 1").executeUpdate();
        em.createNativeQuery("ALTER TABLE account_tb ALTER COLUMN id RESTART WITH 1").executeUpdate();
        em.createNativeQuery("ALTER TABLE transaction_tb ALTER COLUMN id RESTART WITH 1").executeUpdate();

    }

    private void dataSu() {

        // MOCK 객체가 아닌, 실제 DB 에 미리 input 을 넣어주는 것 -
        User mooncake = userRepository.save(makeNewUser("mooncake", "문케이크"));
        User cos = userRepository.save(makeNewUser("cos", "코스,"));
        User love = userRepository.save(makeNewUser("love", "러브"));

        Account mooncakeAccount = accountRepository.save(makeNewAccount(1111L, mooncake));
        Account cosAccount = accountRepository.save(makeNewAccount(2222L, cos));
        Account loveAccount = accountRepository.save(makeNewAccount(3333L, love));

        Transaction withdrawTransaction1 = transactionRepository
                .save(makeWithdrawTransaction(mooncakeAccount, accountRepository));
        Transaction depositTransaction1 = transactionRepository
                .save(makeDepositTransaction(cosAccount, accountRepository));
        Transaction transferTransaction1 = transactionRepository
                .save(makeTransferTransaction(mooncakeAccount, cosAccount, accountRepository));
        Transaction transferTransaction2 = transactionRepository
                .save(makeTransferTransaction(mooncakeAccount, loveAccount, accountRepository));
        Transaction transferTransaction3 = transactionRepository
                .save(makeTransferTransaction(cosAccount, mooncakeAccount, accountRepository));
    }

    /*
     조회 같은 거를 Service 에서 다 TEST 하는건 쫌 어렵긴 함
     DTO 가 엄청 많을 것이기 때문
     차라리 이렇게 Controller 로 print 해서 잘 나오는지 해봐도 되고,
     아니면 부분 검증 하고 정말 확인이 필요한 애들을 기준으로 검증을 해보는 방식
     > 유지 보수가 너무 크게 들지 않는 방식으로 Test 하는게 좋음
     */
    @Test
    @WithUserDetails(value = "mooncake", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void when_requested_should_return_value() throws Exception {

        // given
        Long number = 1111L;
        String txType = "ALL";
        String page = "0";

        // when
        ResultActions resultActions = mvc.perform(get("/api/s/account/" + number + "/transaction")
                .param("transactionType", txType)
                .param("page", page));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("responseBody = " + responseBody);

        // then
        resultActions.andExpect(status().isOk());

        // then 에서 json 검증 하는 방식 (예전에 했었음)
        // 이런식으로 예상 값을 출력해서 matching 해봐도 좋긴 한데.. 이게 정말 괜찮을 까는 좀 의문이긴 하다!

        resultActions.andExpect(jsonPath("$.data.transactions[0].balance").value(900L));
    }
}
