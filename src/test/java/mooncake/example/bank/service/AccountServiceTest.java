package mooncake.example.bank.service;

import mooncake.example.bank.config.dummy.DummyObjectCreator;
import mooncake.example.bank.domain.account.Account;
import mooncake.example.bank.domain.account.AccountRepository;
import mooncake.example.bank.domain.user.User;
import mooncake.example.bank.domain.user.UserRepository;
import mooncake.example.bank.dto.request.AccountReqDto;
import mooncake.example.bank.dto.response.AccountRespDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static mooncake.example.bank.dto.request.AccountReqDto.*;
import static mooncake.example.bank.dto.response.AccountRespDto.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    void 계좌등록_test() throws Exception{
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
        AccountSaveRespDto respDto = accountService.계좌등록(reqDto, userId);

        //t
        assertThat(respDto.getNumber()).isEqualTo(1111L);

    }
}