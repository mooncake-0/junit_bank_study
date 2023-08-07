package mooncake.example.bank.config;

import mooncake.example.bank.domain.account.Account;
import mooncake.example.bank.domain.account.AccountRepository;
import mooncake.example.bank.domain.user.User;
import mooncake.example.bank.domain.user.UserEnum;
import mooncake.example.bank.domain.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

/*
 SAMPLE Data 를
 개발 DB 에 미리 세팅해놓으면 API Testing 에서 편하다
 - (실제 API TESTING or FE 개발자들 TESTING)
 - 방법 외워놓으면 좋을 듯
 */
@Configuration
public class DevDbInit {

    @Bean
    @Profile("dev")
    CommandLineRunner init(UserRepository userRepository, PasswordEncoder passwordEncoder, AccountRepository accountRepository) {
        return (args) -> {
            // 서버 실행시 무조건 실행되는 영역 -> MOCKING 이 아니라 개발 DB에 실제로 들어갈 것이다
            User devSampleUser = User.builder()
                    .username("mooncake")
                    .password(passwordEncoder.encode("1234"))
                    .email("mooncake@naver.com")
                    .fullName("문케이크")
                    .role(UserEnum.CUSTOMER)
                    .build();

            userRepository.save(devSampleUser);

            User secondUser = User.builder()
                    .username("another")
                    .password(passwordEncoder.encode("1234"))
                    .email("another@naver.com")
                    .fullName("어나더")
                    .role(UserEnum.CUSTOMER)
                    .build();

            userRepository.save(secondUser);

            Account account1 = Account.builder()
                    .number(1111L)
                    .password(1234L)
                    .balance(1000L)
                    .user(devSampleUser)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Account account2 = Account.builder()
                    .number(2222L)
                    .password(1234L)
                    .balance(1000L)
                    .user(secondUser)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            accountRepository.save(account1);
            accountRepository.save(account2);
        };
    }
}

