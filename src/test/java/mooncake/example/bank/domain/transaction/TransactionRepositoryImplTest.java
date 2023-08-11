package mooncake.example.bank.domain.transaction;


import mooncake.example.bank.config.dummy.TestObjectCreator;
import mooncake.example.bank.domain.account.Account;
import mooncake.example.bank.domain.account.AccountRepository;
import mooncake.example.bank.domain.user.User;
import mooncake.example.bank.domain.user.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import java.util.List;

@ActiveProfiles("test")
@DataJpaTest // DB 관련된, JPA 관련된 Bean 들을 모두 Container 에 올려준다. // SpringBootTest 보단 가벼움
public class TransactionRepositoryImplTest extends TestObjectCreator { // Transaction 조회를 위해 저장을 해놓는다

    @Autowired
    private EntityManager em;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

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

    @Test
    void findTransactionList_should_return_current_five_transactions() {
        // given - 어쨋든 주어지는 정보. 어떻게 주어지게 되었는진 알바가 아님 // 잘 주어지게 하는건 Controller 의 역할
        Long accountId = 1L;

        // when - local Test 시에는 이렇게 SOUT 도 해보면서 하면 좋음
        List<Transaction> transactions = transactionRepository.findTransactionList(accountId, TransactionEnum.ALL, 0);
        transactions.forEach((t) -> {
            System.out.println("t.getId() = " + t.getId());
            System.out.println("t.getAmount() = " + t.getAmount());
            System.out.println("t.getSender() = " + t.getSender());
            System.out.println("t.getReceiver() = " + t.getReceiver());
            System.out.println("t.getDepositAccountBalance() = " + t.getDepositAccountBalance());
            System.out.println("t.getWithdrawAccountBalance() = " + t.getWithdrawAccountBalance());
            System.out.println(" ================================================================================= ");
        });


        // then ( 내가 주어준 환경 안에서 이루어지니까 그거대로 되는지 확인해도 됨)
        // mooncakeAccount 경우만 마지막 값만 특정 잡아서 Test 걍 해보자.
        // 사실 이렇게 3 번이라는게 .. 맞나..? ㅋㅋㅋㅋㅋㅋㅋㅋ
        // 날짜 순대로 나열하고 Test 하는게 찐 정석이긴 할듯
        Assertions.assertThat(transactions.get(3).getWithdrawAccountBalance()).isEqualTo(1000L);

    }

}

