package mooncake.example.bank.config.dummy;

import mooncake.example.bank.domain.account.Account;
import mooncake.example.bank.domain.account.AccountRepository;
import mooncake.example.bank.domain.transaction.Transaction;
import mooncake.example.bank.domain.transaction.TransactionEnum;
import mooncake.example.bank.domain.user.User;
import mooncake.example.bank.domain.user.UserEnum;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

/*
 MOCKING 객체가 아닌, 실제 메모리 객체를 만들어서 DB 에 넣어준다
 물론 TEST 에서만 사용. ROLLBACK 진행된다
 */
public class TestObjectCreator {

    protected Transaction makeWithdrawTransaction(Account withdrawAccount, AccountRepository accountRepository) {
        // Transaction 이 생기기 위해선 진짜 Tx 가 이루어져야 맞음
        withdrawAccount.withdraw(100L);

        // 더티체킹 안됨 (영속화 클리어 상태) -> TODO : 근데 개인적인 논란이 있음
        if (accountRepository != null) {
            accountRepository.save(withdrawAccount);
        }

        return Transaction.builder()
                .transactionType(TransactionEnum.WITHDRAW)
                .depositAccount(null)
                .depositAccountBalance(null)
                .withdrawAccount(withdrawAccount)
                .withdrawAccountBalance(withdrawAccount.getBalance())
                .amount(100L)
                .sender(withdrawAccount.getNumber() + "") // WithdrawAccount 주인한테서 빠지는거니, 주인이 보내는거임
                .receiver("ATM")
                .build();
    }

    protected Transaction makeDepositTransaction(Account depositAccount, AccountRepository accountRepository) {
        // Transaction 이 생기기 위해선 진짜 Tx 가 이루어져야 맞음
        depositAccount.deposit(100L);

        // 더티체킹 안됨 (영속화 클리어 상태) -> TODO : 근데 개인적인 논란이 있음
        if (accountRepository != null) {
            accountRepository.save(depositAccount);
        }

        return Transaction.builder()
                .transactionType(TransactionEnum.DEPOSIT)
                .depositAccount(depositAccount)
                .depositAccountBalance(depositAccount.getBalance())
                .withdrawAccount(null)
                .withdrawAccountBalance(null)
                .amount(100L)
                .sender("ATM") // WithdrawAccount 주인한테서 빠지는거니, 주인이 보내는거임
                .receiver(depositAccount.getNumber() + "")
                .build();
    }


    protected Transaction makeTransferTransaction(Account depositAccount, Account withdrawAccount, AccountRepository accountRepository) {

        withdrawAccount.withdraw(100L);
        depositAccount.deposit(100L);

        if (accountRepository != null) {
            accountRepository.save(withdrawAccount);
            accountRepository.save(depositAccount);
        }

        return Transaction.builder()
                .transactionType(TransactionEnum.TRANSFER)
                .depositAccount(depositAccount)
                .depositAccountBalance(depositAccount.getBalance())
                .withdrawAccount(withdrawAccount)
                .withdrawAccountBalance(withdrawAccount.getBalance())
                .amount(100L)
                .sender(withdrawAccount.getNumber() + "") // WithdrawAccount 주인한테서 빠지는거니, 주인이 보내는거임
                .receiver(depositAccount.getNumber() + "")
                .build();
    }


    protected User makeNewUser(String username, String fullName) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return User.builder()
                .username(username)
                .password(encoder.encode("1234"))
                .fullName(fullName)
                .email(username + "hi.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .role(UserEnum.CUSTOMER)
                .build();
    }

    protected Account makeNewAccount(Long number, User user) {
        return Account.builder()
                .number(number)
                .password(1234L)
                .balance(1000L)
                .user(user)
                .build();
    }

}
