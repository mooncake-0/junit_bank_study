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


    @Transactional
    public AccountWithdrawRespDto 계좌출금(AccountWithdrawReqDto requestDto, Long requestUserId) {

        if (requestDto.getAmount() <= 0L) { // 0 원 체크
            throw new CustomApiException("0원 이하의 금액을 출금할 수 없습니다");
        }

        // 출금 계좌 확인
        Account accountPs = accountRepository.findByNumber(requestDto.getNumber()).orElseThrow(() -> new CustomApiException("계좌를 찾을 수 없습니다"));

        // 출금 소유자 확인 (요청자와 동일한지)
        accountPs.checkOwner(requestUserId);

        // 출금 계좌 비번 확인
        accountPs.checkPassword(requestDto.getPassword());

        // 이제서야 비로소 출금
        accountPs.withdraw(requestDto.getAmount());

        // 거래내역 남기기
        Transaction withDrawTx = Transaction.builder()
                .withdrawAccount(accountPs)
                .depositAccount(null)
                .depositAccountBalance(null)
                .withdrawAccountBalance(accountPs.getBalance())
                .amount(requestDto.getAmount())
                .transactionType(TransactionEnum.WITHDRAW)
                .sender(requestDto.getNumber() + "")
                .receiver("ATM")
                .build();

        Transaction txPs = transactionRepository.save(withDrawTx);

        // DTO 응답
        return new AccountWithdrawRespDto(accountPs, new TransactionDto(withDrawTx));

    }


    // 계좌이체 Service 샘플
    public AccountTransferRespDto 계좌이체(AccountTransferReqDto requestDto, Long userId) {

        // 출금계좌와 입금계좌가 동일하지 않은지도 확인하래
        if (Objects.equals(requestDto.getDepositNumber(), requestDto.getWithDrawNumber())) {
            throw new CustomApiException("출금 / 입금 계좌가 같을 수 없습니다");
        }

        if (requestDto.getAmount() <= 0L) { // 0 원 체크
            throw new CustomApiException("0원 이하의 금액을 이체할 수 없습니다");
        }

        // 각 계좌 확인
        // 출금 계좌 확인
        Account withdrawAccountPs = accountRepository.findByNumber(requestDto.getWithDrawNumber()).orElseThrow(() -> new CustomApiException("출금 계좌를 찾을 수 없습니다"));
        withdrawAccountPs.checkOwner(userId);
        withdrawAccountPs.checkPassword(requestDto.getWithDrawPassword());

        // 출금 계좌 확인
        Account depositAccountPs = accountRepository.findByNumber(requestDto.getDepositNumber()).orElseThrow(() -> new CustomApiException("입금할 계좌를 찾을 수 없습니다"));

        // 이체 로직 (중요한 Tx 보장 부분)
        withdrawAccountPs.withdraw(requestDto.getAmount());
        depositAccountPs.deposit(requestDto.getAmount());

        // 거래내역 남기기
        Transaction transferTx = Transaction.builder()
                .withdrawAccount(withdrawAccountPs)
                .depositAccount(depositAccountPs)
                .depositAccountBalance(depositAccountPs.getBalance())
                .withdrawAccountBalance(withdrawAccountPs.getBalance())
                .amount(requestDto.getAmount())
                .transactionType(TransactionEnum.TRANSFER)
                .sender(withdrawAccountPs.getNumber() + "")
                .receiver(depositAccountPs.getNumber() + "")
                .build();

        transactionRepository.save(transferTx);

        return new AccountTransferRespDto(withdrawAccountPs, new TransactionDto(transferTx));

    }

    // 계좌의 상세정보, 그리고 최근 이체 내역 까지 모두 전달해준다
    public AccountDetailRespDto 계좌상세보기(Long number, Long userId, Integer page) {
        // 구분값 , 페이지 고정
        String txType = "ALL"; // 조회하려는 계좌의 모든 내역이 필요하기 때문 // 고정값으로 요청을 처리하기 때문

        Account accountPs = accountRepository.findByNumber(number).orElseThrow(() -> new CustomApiException("계좌를 찾을 수 없습니다"));

        accountPs.checkOwner(userId);

        List<Transaction> transactionList = transactionRepository.findTransactionList(accountPs.getId(), TransactionEnum.valueOf(txType), page);

        return new AccountDetailRespDto(accountPs, transactionList);
    }

}