package mooncake.example.bank.service;

import mooncake.example.bank.config.dummy.DummyObjectCreator;
import mooncake.example.bank.domain.account.Account;
import mooncake.example.bank.domain.account.AccountRepository;
import mooncake.example.bank.domain.transaction.Transaction;
import mooncake.example.bank.domain.transaction.TransactionEnum;
import mooncake.example.bank.domain.transaction.TransactionRepository;
import mooncake.example.bank.domain.user.User;
import mooncake.example.bank.domain.user.UserRepository;
import mooncake.example.bank.dto.response.TransactionRespDto;
import mooncake.example.bank.handler.exception.CustomApiException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static mooncake.example.bank.dto.request.AccountReqDto.*;
import static mooncake.example.bank.dto.response.AccountRespDto.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;



/*
 Test 대상 외에 함수 로직은
 모두 stub 처리가 되어야 한다
 - 사용되는 객체들은 Test 대상 외는 Mocking 바보 객체로 만들어져야 하는데,
 - 여기서 Mock.class 로 사용되든지 아니면 실 객체로 사용하든지는 선택인듯
 - 나는 실 객체로 하는게 차피 getter 를 자동으로 사용할 수 있기 때문에 그냥 쓰는게 나을듯
 - Domain Entity 객체에서는.. 굳이 문제가 연동되는거까진 신경 안써도 되지 않을까 싶음
 */
@ActiveProfiles(value = "test")
@ExtendWith(MockitoExtension.class)
public class AccountServiceTest extends DummyObjectCreator {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository txRepository;

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

    // Validation Test (AOP 부분) 는 Controller 테스트에서 MvcMock 환경에서 해야하는 듯
    @Test
    void when_right_request_should_deposit_on_account() throws Exception {

        // given
        Long number = 1111L;
        Long password = 1234L;
        User mockUser = newMockUser(1L, "mooncake", "문케이크");
        Account mockAccount = newMockAccount(1L, number, password, mockUser);
        Long preBalance= mockAccount.getBalance();

        //given - 2
        AccountDepositReqDto requestDto = new AccountDepositReqDto();
        requestDto.setNumber(number);
        requestDto.setTel("01012345678");
        requestDto.setTransactionType(TransactionEnum.DEPOSIT.getValue());
        requestDto.setAmount(100L);
        // 이씨발 이거 왜 "입금" 으로 들어가는데 안잡아줘 ㅆㅂ 이거 문제여 ㅆㅂ ㅆㅃㅆㅃㅆㅃㅆㅄㅃㅆㅃㅆㅃㅆ
        // given - 3 stub
        Transaction txMock = newDepositMockTransaction(1L, mockAccount, requestDto);
        when(accountRepository.findByNumber(number)).thenReturn(Optional.of(mockAccount));
        when(txRepository.save(any())).thenReturn(txMock);

        // when
        AccountDepositRespDto response = accountService.계좌입금(requestDto);
        TransactionRespDto.TransactionDto txResponse = response.getTransactionDto();

        // then
        assertThat(response.getId()).isEqualTo(mockAccount.getId());
        assertThat(response.getNumber()).isEqualTo(mockAccount.getNumber());
        assertThat(txResponse.getId()).isEqualTo(txMock.getId());
        assertThat(txResponse.getTransactionType()).isEqualTo(TransactionEnum.DEPOSIT.getValue());
        assertThat(txResponse.getSender()).isEqualTo("ATM");
        assertThat(txResponse.getAmount()).isEqualTo(requestDto.getAmount());

        // then - Deposit 에 따른 확인 - Balance 변경 내역을 확인해보자 (실제 로직의 결과물 확인이 중요)
        assertThat(mockAccount.getBalance()).isEqualTo(preBalance + requestDto.getAmount());
        assertThat(txResponse.getDepositAccountBalance()).isEqualTo(txMock.getDepositAccountBalance());
    }


    @Test
    void when_0_amount_transferred_should_throw_error() throws Exception {

        // given
        Long number = 1111L;
        Long password = 1234L;
        User mockUser = newMockUser(1L, "mooncake", "문케이크");
        Account mockAccount = newMockAccount(1L, number, password, mockUser);
        Long preBalance= mockAccount.getBalance();

        //given - 2
        AccountDepositReqDto requestDto = new AccountDepositReqDto();
        requestDto.setNumber(number);
        requestDto.setTel("01012345678");
        requestDto.setTransactionType(TransactionEnum.DEPOSIT.getValue());
        requestDto.setAmount(0L);

        // when
        // then
        assertThatThrownBy(() -> accountService.계좌입금(requestDto)).isInstanceOf(CustomApiException.class);
    }

    @Test
    void when_right_request_should_withdraw_from_account() throws Exception {

        // given (행위의 주체) Account, User Mock 이 필요하다
        User mockUser = newMockUser(1L, "mooncake", "문케이크");
        Account mockAccount = newMockAccount(1L, 1111L, 1234L, mockUser);
        Long preBalance = mockAccount.getBalance();

        //given (행위를 객체화 한 것 -> RequestDto )
        AccountWithdrawReqDto requestDto = new AccountWithdrawReqDto();
        requestDto.setNumber(1111L);
        requestDto.setPassword(1234L);
        requestDto.setAmount(100L);
        requestDto.setTranasctionType(TransactionEnum.WITHDRAW.toString());

        // given - account repo 무시를 위한 stub
        when(accountRepository.findByNumber(any())).thenReturn(Optional.of(mockAccount));

        // given - transaction repo 무시를 위한 stub (굳이굳이 저장된 애를 DTO로 만들라고 넣었기 때문에 Tx 반환 stubbing 해줘야 함)
        // 근데 생각해보면 내가 저장할 객체의 id를 확인할거 아니면 만든게 PS 에 들어가게 되므로, 그냥 만들어놓지 않고 생성되게 하겠음

        //when
        AccountWithdrawRespDto responseDto = accountService.계좌출금(requestDto, 1L);

        //then
        assertThat(responseDto.getBalance()).isEqualTo(preBalance - requestDto.getAmount());
    }


    @Test
    void when_right_request_should_transfer_amount_to_other_account() throws Exception {

        // given (행위의 주체) 입금 계좌, 출금 계좌, 입금자, 출금자
        User sender = newMockUser(1L, "sender", "센더");
        User receiver = newMockUser(2L, "receiver", "리시버");
        Account withdrawAccount = newMockAccount(1L, 1111L, 1234L, sender);
        Account depositAccount = newMockAccount(2L, 2222L, 2345L, receiver);
        Long preWithAccountBalance = withdrawAccount.getBalance();
        Long preDepAccountBalance = depositAccount.getBalance();

        // given
        AccountTransferReqDto reqDto = new AccountTransferReqDto();
        reqDto.setAmount(100L);
        reqDto.setWithDrawNumber(1111L);
        reqDto.setWithDrawPassword(1234L);
        reqDto.setDepositNumber(2222L);
        reqDto.setTransactionType(TransactionEnum.TRANSFER.toString());

        // given - repository 무시를 위한 필요값 지정
        when(accountRepository.findByNumber(withdrawAccount.getNumber())).thenReturn(Optional.of(withdrawAccount));
        when(accountRepository.findByNumber(depositAccount.getNumber())).thenReturn(Optional.of(depositAccount));

        // when
        AccountTransferRespDto respDto = accountService.계좌이체(reqDto, 1L);// 요청자가  sender 유저임

        // then (출금자가 로그인한 유저이므로, 출금자 기준으로 Test를 한다)
        assertThat(respDto.getBalance()).isEqualTo(preWithAccountBalance - reqDto.getAmount());
        assertThat(respDto.getTransactionDto().getDepositAccountBalance()).isEqualTo(preDepAccountBalance + reqDto.getAmount());

    }


}