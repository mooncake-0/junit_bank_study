package mooncake.example.bank.service;

import mooncake.example.bank.config.dummy.DummyObjectCreator;
import mooncake.example.bank.domain.account.Account;
import mooncake.example.bank.domain.account.AccountRepository;
import mooncake.example.bank.domain.user.User;
import mooncake.example.bank.domain.user.UserRepository;
import mooncake.example.bank.handler.exception.CustomApiException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static mooncake.example.bank.dto.request.AccountReqDto.*;
import static mooncake.example.bank.dto.response.AccountRespDto.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest extends DummyObjectCreator {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Test
    @DisplayName("Account 등록에 성공한다")
    void 계좌등록_test() throws Exception {
        //g
        AccountSaveReqDto reqDto = new AccountSaveReqDto();
        reqDto.setNumber(1111L);
        reqDto.setPassword(1234L);

        Long userId = 1L;
        User mockedUser = newMockUser(userId, "mooncake", "문케이크");
        when(userRepository.findById(any())).thenReturn(Optional.of(mockedUser));
        when(accountRepository.findByNumber(any())).thenReturn(Optional.empty()); // 성공 CASE 여야 한다

        Account mockedAccount = newMockAccount(1L, reqDto.getNumber(), reqDto.getPassword(), mockedUser);
        when(accountRepository.save(any())).thenReturn(mockedAccount);

        //w
        AccountNormalRespDto respDto = accountService.계좌등록(reqDto, userId);

        //t
        assertThat(respDto.getNumber()).isEqualTo(1111L);

    }

    @Test
    void when_right_input_given_should_not_throw_exception() throws Exception {
        // given
        Long number = 1111L;
        Long userId = 1L;

        // given - stub
        User mockedUser = newMockUser(userId, "mooncake", "문케이크"); // 얘가 요청하는거라고 함 그리고 얘의 계좌임
        Account mockedAccount = newMockAccount(1L, number, 1234L, mockedUser);
        when(accountRepository.findByNumber(any())).thenReturn(Optional.of(mockedAccount));

        // when
        // then
        assertDoesNotThrow(() -> accountService.계좌삭제(number, userId));

    }

    @Test
    void when_wrong_user_request_account_removal_should_throw_exception() throws Exception {
        // given
        Long number = 1111L;
        Long userId = 2L;

        // given - stub
        User mockedUser = newMockUser(1L, "mooncake", "문케이크"); // 얘가 요청하는거라고 함 그리고 얘의 계좌임
        Account mockedAccount = newMockAccount(1L, number, 1234L, mockedUser);
        when(accountRepository.findByNumber(any())).thenReturn(Optional.of(mockedAccount));

        // when
        // then
        assertThatThrownBy(() -> accountService.계좌삭제(number, userId)).isInstanceOf(CustomApiException.class);

    }

}