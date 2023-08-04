package mooncake.example.bank.dto.response;

import lombok.Getter;
import lombok.Setter;
import mooncake.example.bank.domain.account.Account;

public class AccountRespDto {

    @Getter
    @Setter
    public static class AccountSaveRespDto {
        public Long id;
        public Long number;
        private Long balance;

        public AccountSaveRespDto(Account account) {
            this.id = account.getId();
            this.number = account.getNumber();
            this.balance = account.getBalance();
        }
    }
}
