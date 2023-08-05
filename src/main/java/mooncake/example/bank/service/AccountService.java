package mooncake.example.bank.service;

import lombok.RequiredArgsConstructor;
import mooncake.example.bank.domain.account.Account;
import mooncake.example.bank.domain.account.AccountRepository;
import mooncake.example.bank.domain.user.User;
import mooncake.example.bank.domain.user.UserRepository;
import mooncake.example.bank.handler.exception.CustomApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static mooncake.example.bank.dto.request.AccountReqDto.*;
import static mooncake.example.bank.dto.response.AccountRespDto.*;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AccountService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

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

}