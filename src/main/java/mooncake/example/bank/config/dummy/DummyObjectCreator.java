package mooncake.example.bank.config.dummy;

import mooncake.example.bank.domain.account.Account;
import mooncake.example.bank.domain.user.User;
import mooncake.example.bank.domain.user.UserEnum;
import mooncake.example.bank.dto.request.AccountReqDto;
import mooncake.example.bank.dto.request.UserReqDto;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static mooncake.example.bank.dto.request.AccountReqDto.*;
import static mooncake.example.bank.dto.response.UserRespDto.*;
import static mooncake.example.bank.dto.request.UserReqDto.*;

/*
 MEMO :: TEST - MOCKING 환경 용 MOCKING 객체들은 ID 까지 직접 넣어줘야 ID도 Verify 가 가능하다
 */
public class DummyObjectCreator {

    protected Account newMockAccount(Long id, Long number, Long password, User user) {
        Account mockAccount = Account.builder()
                .id(id)
                .number(number)
                .password(password)
                .balance(1000L)
                .user(user).build();

        return mockAccount;
    }

    protected LoginReqDto newMockLoginDto(String username, String password) {
        LoginReqDto ld = new LoginReqDto();
        ld.setUsername(username);
        ld.setPassword(password);
        return ld;
    }

    protected UserJoinReqDto newMockRequestDto(String username, String password, String email, String fullName) {
        return new UserJoinReqDto(username, password, email, fullName);
    }

    protected AccountSaveReqDto newMockAccountSaveReqDto(Long number, Long password) {
        AccountSaveReqDto accountSaveReqDto = new AccountSaveReqDto();
        accountSaveReqDto.setNumber(number);
        accountSaveReqDto.setPassword(password);

        return accountSaveReqDto;
    }

    protected User newMockUser(Long id, String username, String fullName) {

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPw = passwordEncoder.encode("1234");

        return User.builder()
                .id(id)
                .username(username)
                .password(encodedPw)
                .email(username + "@hello.com")
                .fullName(fullName)
                .role(UserEnum.CUSTOMER)
                .build();

    }
}
