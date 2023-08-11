package mooncake.example.bank.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class AccountReqDto {

    @Getter
    @Setter
    public static class AccountWithdrawReqDto { // 계좌 출금을 위함

        @NotNull
        @Digits(integer = 4, fraction = 4)
        private Long number;
        @NotNull
        @Digits(integer = 4, fraction = 4)
        private Long password;
        @NotNull
        private Long amount;

        @NotEmpty
        @Pattern(regexp = "WITHDRAW") // 일반식 표현은.. 지피티랑 해
        private String tranasctionType;
    }

    @Getter
    @Setter
    public static class AccountTransferReqDto{


        @NotNull
        @Digits(integer = 4, fraction = 4)
        private Long withDrawNumber;

        @NotNull
        @Digits(integer = 4, fraction = 4)
        private Long withDrawPassword;

        @NotNull
        @Digits(integer = 4, fraction = 4)
        private Long depositNumber;

        @NotNull
        private Long amount;

        @NotEmpty
        @Pattern(regexp = "TRANSFER") // 일반식 표현은.. 지피티랑 해
        private String transactionType; // Deposit

    }

    @Getter
    @Setter
    public static class AccountSaveReqDto {
        @org.jetbrains.annotations.NotNull
        @Digits(integer = 4, fraction = 4)
        private Long number;

        @NotNull
        @Digits(integer = 4, fraction = 4) // 최소 4자리, 최대 4자리
        private Long password;
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
        @Pattern(regexp = "^[0-9]{11}")
        private String tel;
    }
}
