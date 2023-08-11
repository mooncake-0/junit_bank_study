package mooncake.example.bank.dto.response;

import lombok.Getter;
import lombok.Setter;
import mooncake.example.bank.domain.account.Account;
import mooncake.example.bank.domain.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static mooncake.example.bank.dto.response.TransactionRespDto.*;

public class AccountRespDto {


    // 똑같아도 재사용 금지!!!!! DTO 는 정말 요청 구분별로 다 해야하는게 맞음!!
    @Getter
    @Setter
    public static class AccountWithdrawRespDto{

        private Long id;
        private Long number;
        private Long balance; // 잔여 금액
        private TransactionDto transactionDto;

        public AccountWithdrawRespDto(Account account, TransactionDto transactionDto) {
            this.id = account.getId();
            this.number = account.getNumber();
            this.balance = account.getBalance();
            this.transactionDto = transactionDto;
        }
    }


    @Getter
    @Setter
    public static class AccountTransferRespDto{

        private Long id;
        private Long number; // 출금 계좌번호
        private Long balance; // 잔여 금액
        private TransactionDto transactionDto; // 이체 내역

        public AccountTransferRespDto(Account account, TransactionDto transactionDto) {
            this.id = account.getId();
            this.number = account.getNumber();
            this.balance = account.getBalance();
            this.transactionDto = transactionDto;
        }
    }
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

    @Getter
    @Setter
    public static class AccountDetailRespDto {

        private Long id;
        private Long number;
        private Long balance; // 그 계좌의 현재 잔액
        private List<TransactionViewDto> transactions = new ArrayList<>(); // Inner Domain DTO 들은 꼮 따로 생성

        public AccountDetailRespDto(Account account, List<Transaction> rawTx) {
            this.id = account.getId();
            this.number = account.getNumber();
            this.balance = account.getBalance();
            this.transactions = rawTx.stream().map(tx -> new TransactionViewDto(tx, account.getNumber()))
                    .collect(Collectors.toList());
        }

        /*
         DTO 는 똑같더라도 요청별로 분리 ㅊㅊ..
         재사용하지 않는걸 추천한다..
         ㅈㄴ 헷갈림
         */
        @Getter
        @Setter
        public class TransactionViewDto {

            private Long id;
            private String transactionType;
            private Long amount;
            private String sender;
            private String receiver;
            private String tel;
            private String createdAt;
            private Long balance;

            public TransactionViewDto(Transaction transaction, Long accountNumber) {
                this.id = transaction.getId();
                this.transactionType = transaction.getTransactionType().getValue();
                this.amount = transaction.getAmount();
                this.sender = transaction.getSender();
                this.receiver = transaction.getReceiver();
                this.createdAt = transaction.getCreatedAt() + "";
                this.tel = transaction.getTel() == null ? "없음" : transaction.getTel();

                if (transaction.getDepositAccount() == null) {
                    this.balance = transaction.getWithdrawAccountBalance();
                } else if (transaction.getWithdrawAccount() == null) {
                    this.balance = transaction.getDepositAccountBalance();
                } else {
                    // 입출금 내역조회임 --> 이 쪽이면.. 입출금 내역 조회하겠다는 뜻
                    // 둘 다 들어올 수 있으니 두개로 나눠서 해줘야함
                    if (accountNumber.longValue() == transaction.getDepositAccount().getNumber()) { // 이번 Tx 가 deposit 이면 deposit 에
                        this.balance = transaction.getDepositAccountBalance();
                    } else {
                        this.balance = transaction.getWithdrawAccountBalance();
                    }
                }
            }
        }
    }
}
