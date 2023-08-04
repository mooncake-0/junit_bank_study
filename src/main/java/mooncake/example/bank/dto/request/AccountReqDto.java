package mooncake.example.bank.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

public class AccountReqDto {

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
}
