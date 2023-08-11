package mooncake.example.bank.web;

import lombok.RequiredArgsConstructor;
import mooncake.example.bank.config.security.auth.LoginUser;
import mooncake.example.bank.dto.ResponseDto;
import mooncake.example.bank.dto.response.TransactionRespDto;
import mooncake.example.bank.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static mooncake.example.bank.dto.response.TransactionRespDto.*;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/s/account/{number}/transaction")
    public ResponseEntity<?> findTransactionList(@PathVariable Long number, // 여기서부턴 뒤에  RequestParam 으로 들어오게 됨
                                                 @RequestParam(value = "transactionType", defaultValue = "ALL") String transactionType,
                                                 @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                 @AuthenticationPrincipal LoginUser loginUser) {

        TransactionListRespDto respDto = transactionService.입출금목록보기(loginUser.getUser().getId(), number, transactionType, page);


        return ResponseEntity.ok().body(new ResponseDto<>(1, "목록 전송 선공", respDto));

    }
}