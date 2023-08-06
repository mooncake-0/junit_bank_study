package mooncake.example.bank.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import mooncake.example.bank.domain.account.Account;
import mooncake.example.bank.domain.account.AccountRepository;
import mooncake.example.bank.domain.transaction.Transaction;
import mooncake.example.bank.domain.transaction.TransactionEnum;
import mooncake.example.bank.domain.transaction.TransactionRepository;
import mooncake.example.bank.domain.user.User;
import mooncake.example.bank.domain.user.UserRepository;
import mooncake.example.bank.dto.response.TransactionRespDto;
import mooncake.example.bank.handler.exception.CustomApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static mooncake.example.bank.dto.request.AccountReqDto.*;
import static mooncake.example.bank.dto.response.AccountRespDto.*;
import static mooncake.example.bank.dto.response.TransactionRespDto.*;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AccountService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    private final TransactionRepository transactionRepository;

    @Transactional
    public AccountNormalRespDto 계좌등록(AccountSaveReqDto requestDto, Long userId) {
        // USER 가 DB 에 있는지 검증
        User userPs = userRepository.findById(userId).orElseThrow(
                () -> new CustomApiException("유저를 찾을 수 없습니다")
        );

        // 해당 계좌가 DB에 있는지 중복 검증
        Optional<Account> accoutOp = accountRepository.findByNumber(requestDto.getNumber());
        if (accoutOp.isPresent()) {
            throw new CustomApiException("해당 계좌가 이미 존재합니다");
        }

        // 계좌 등록
        Account accountPs = accountRepository.save(Account.builder()
                .number(requestDto.getNumber())
                .password(requestDto.getPassword())
                .balance(1000L)
                .user(userPs)
                .build());

        // DTO 로 응답
        return new AccountNormalRespDto(accountPs);
    }


    public AccountListRespDto 계좌목록보기_유저별(Long userId) {

        User userPs = userRepository.findById(userId).orElseThrow(() -> new CustomApiException("유저를 찾을 수 없습니다"));

        // 유저의 모든 계좌 목록
        List<Account> userAccounts = accountRepository.findByUser_id(userId);

        // 반환
        return new AccountListRespDto(userPs.getFullName(), userAccounts);

    }

    @Transactional
    public void 계좌삭제(Long number, Long userId) {
        // 계좌 확인
        Account accountPs = accountRepository.findByNumber(number).orElseThrow(() -> new CustomApiException("해당 번호의 계좌를 찾을 수 없습니다"));

        // 소유주 일치 확인
        accountPs.checkOwner(userId);

        // 삭제 진행
        accountRepository.deleteById(accountPs.getId());
    }


    // 인증이 필요 없다..?
    @Transactional
    public AccountDepositRespDto 계좌입금(AccountDepositReqDto requestDto) {
        // 0 원 체크
        if (requestDto.getAmount() <= 0L) {
            throw new CustomApiException("0원 이하의 금액을 입금할 수 없습니다");
        }

        // 입금 계좌 확인
        Account accountPs = accountRepository.findByNumber(requestDto.getNumber()).orElseThrow(() -> new CustomApiException("계좌를 찾을 수 없습니다"));

        // 입금 (해당 계좌 balance 조정 - update 문 - 알아서 더티체킹)
        accountPs.deposit(requestDto.getAmount());

        // 거래 내역 남기기 (TX가 완료되는 시점에서 진행되어야 한다)
        Transaction txNow = Transaction.builder()
                .depositAccount(accountPs)
                .withdrawAccount(null) // 입금은 - 되는 사람 정보는 넣지 않는다 (정확한 이체만 등재)
                .depositAccountBalance(accountPs.getBalance())
                .withdrawAccountBalance(null) // 그래서 이것도 없다
                .amount(requestDto.getAmount())
                .transactionType(TransactionEnum.DEPOSIT)
                .sender("ATM")
                .receiver(requestDto.getNumber() + "")
                .tel(requestDto.getTel())
                .build();


        Transaction txPs = transactionRepository.save(txNow);
        return new AccountDepositRespDto(accountPs, new TransactionDto(txPs));

    }

    @Getter
    @Setter
    public static class AccountDepositReqDto { // 누구의 계좌에서 얼마만큼의 금액 중 무슨 Type 의 일을 발생시킬 것이냐

        @NotNull
        @Digits(integer = 4, fraction = 4)
        private Long number;

        @NotNull
        private Long amount; // 0 원 유효성 검사 여기서 해도 됨. 여기서 하는게 사실 맞음

        @NotEmpty
        @Pattern(regexp = "DEPOSIT") // 일반식 표현은.. 지피티랑 해
        private String transactionType; // Deposit
        @NotEmpty
        @Pattern(regexp = "^[0-9]{3}[0-9]{4}[0-9]{4}")
        private String tel;
    }

    @Getter
    @Setter
    public static class AccountDepositRespDto{

        private Long id;
        private Long number;
        private TransactionDto transactionDto;

        public AccountDepositRespDto(Account account, TransactionDto transactionDto) {
            this.id = account.getId();
            this.number = account.getNumber();
            this.transactionDto = transactionDto;
        }


    }

}