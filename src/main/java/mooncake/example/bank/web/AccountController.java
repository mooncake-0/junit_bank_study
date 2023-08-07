package mooncake.example.bank.web;

import lombok.RequiredArgsConstructor;
import mooncake.example.bank.config.security.auth.LoginUser;
import mooncake.example.bank.dto.ResponseDto;
import mooncake.example.bank.dto.request.AccountReqDto;
import mooncake.example.bank.dto.response.AccountRespDto;
import mooncake.example.bank.handler.exception.CustomApiException;
import mooncake.example.bank.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static mooncake.example.bank.dto.request.AccountReqDto.*;
import static mooncake.example.bank.dto.response.AccountRespDto.*;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/s/account")
    public ResponseEntity<?> createAccount(@RequestBody @Valid AccountSaveReqDto requestDto, BindingResult bindingResult,
                                           @AuthenticationPrincipal LoginUser loginUser) { // SecurityContext 안에 있는 Authentication.getPrincipal 객체를 반환
        AccountNormalRespDto responseDto = accountService.계좌등록(requestDto, loginUser.getUser().getId());
        return new ResponseEntity<>(new ResponseDto<>(1, "계좌등록성공", responseDto), HttpStatus.CREATED);
    }

    // 이런식으로 주석을 달면 더 안전하게 할 수 있음
    // {id 는 JWT 토큰으로 확인되기 때문에 필요 없음}
    // 그렇다고 /s/account - 이건 모든 계좌를 주세요 이런요청임
    // 따라서 /s/account/login-user - 이런 식으로 명시해놓으면 됨
//    @GetMapping("/s/account/{id}")
    @GetMapping("/s/account/login-user")
    public ResponseEntity<?> findUserAccount(@AuthenticationPrincipal LoginUser loginUser) {

//         추가적 검증로직 (결국 테스트할 것만 많아짐 분기랑.. 그냥 계속 줄일 수 있으면 줄여야함)
//        if (id != loginUser.getUser().getId()) {
//            throw new CustomApiException("잘못된 요청입니다.");
//        }

        AccountListRespDto respDto = accountService.계좌목록보기_유저별(loginUser.getUser().getId());
        return new ResponseEntity<>(new ResponseDto<>(1, "계좌목록보기 유저 성공", respDto), HttpStatus.OK);
    }

    @DeleteMapping("/s/account/{number}")
    public ResponseEntity<?> deleteAccount(@PathVariable Long number, @AuthenticationPrincipal LoginUser loginUser) {
        System.out.println("B DEBUGGING " + loginUser.getUser().getId());
        accountService.계좌삭제(number, loginUser.getUser().getId());
        return new ResponseEntity<>(new ResponseDto<>(1, "계좌 삭제 완료", null), HttpStatus.OK);
    }


    @PostMapping("/account/deposit")
    public ResponseEntity<?> depositAccount(@RequestBody @Valid AccountService.AccountDepositReqDto requestDto, BindingResult bindingResult) {
        AccountService.AccountDepositRespDto responseDto = accountService.계좌입금(requestDto);
        return new ResponseEntity<>(new ResponseDto<>(1, "계좌 입금 완료 ", responseDto), HttpStatus.CREATED); //TX 생성

    }
}

