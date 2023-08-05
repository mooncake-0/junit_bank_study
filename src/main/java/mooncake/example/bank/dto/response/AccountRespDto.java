package mooncake.example.bank.dto.response;

import lombok.Getter;
import lombok.Setter;
import mooncake.example.bank.domain.account.Account;

import java.util.ArrayList;
import java.util.List;

public class AccountRespDto {

    @Getter
    @Setter
    public static class AccountNormalRespDto {
        public Long id;
        public Long number;
        private Long balance;

        public AccountNormalRespDto(Account account) {
            this.id = account.getId();
            this.number = account.getNumber();
            this.balance = account.getBalance();
        }
    }

    @Getter
    @Setter
    public static class AccountListRespDto{
        private String fullName; // user 응답
        private List<AccountNormalRespDto> accounts = new ArrayList<>();

        public AccountListRespDto(String fullName, List<Account> realAccounts) {
            this.fullName = fullName;
            for (Account account : realAccounts) {
                this.accounts.add(new AccountNormalRespDto(account));
            }
        }
    }
}
