package mooncake.example.bank.service;


import lombok.RequiredArgsConstructor;
import mooncake.example.bank.domain.account.Account;
import mooncake.example.bank.domain.account.AccountRepository;
import mooncake.example.bank.domain.transaction.Transaction;
import mooncake.example.bank.domain.transaction.TransactionEnum;
import mooncake.example.bank.domain.transaction.TransactionRepository;
import mooncake.example.bank.dto.response.TransactionRespDto;
import mooncake.example.bank.handler.exception.CustomApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static mooncake.example.bank.dto.response.TransactionRespDto.*;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;

    private final AccountRepository accountRepository;

    // 로그인 한 사람만 받아볼 수 있음
    public TransactionListRespDto 입출금목록보기(Long userId, Long accountNumber, String transactionType, int page) {


        Account accountPs = accountRepository.findByNumber(accountNumber).orElseThrow(
                () -> new CustomApiException("계좌를 찾을 수 없습니다")
        );

        accountPs.checkOwner(userId); // 로그인 정보를 가져오는 것 // Test 할게 얘밖에 없음

        List<Transaction> transactions = transactionRepository.findTransactionList(accountPs.getId(), TransactionEnum.valueOf(transactionType), page);

        return new TransactionListRespDto(accountPs, transactions);
    }

}
