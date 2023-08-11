package mooncake.example.bank.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import mooncake.example.bank.domain.account.Account;
import mooncake.example.bank.domain.transaction.Transaction;
import mooncake.example.bank.domain.transaction.TransactionEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionRespDto {

    @Getter
    @Setter
    public static class TransactionDto {
        private Long id;
        private String transactionType;
        private String sender;
        private String receiver;

        private Long amount;
        private String tel;
        private String createdAt;

        @JsonIgnore // 응답에 보낼 값은 아니다. 다만, 내가 Test 를 위해 사용하겠다는 뜻 (그 용도로 보통 사용)
        private Long depositAccountBalance;

        public TransactionDto(Transaction transaction) {
            this.id = transaction.getId();
            this.transactionType = transaction.getTransactionType().getValue();
            this.sender = transaction.getSender();
            this.receiver = transaction.getReceiver();
            this.amount = transaction.getAmount();
            this.depositAccountBalance = transaction.getDepositAccountBalance();
            this.tel = transaction.getTel();
            this.createdAt = transaction.getCreatedAt() + "";
        }
    }

    @Getter
    @Setter
    public static class TransactionListRespDto { // List 제공을 위함
        private List<TransactionViewDto> transactions = new ArrayList<>();

        // 이렇게 여러 DTO에서 사용하는 애들은 보낼때마다 기준이 달라질 수도 있음
        // 공용 말고 Resp Model 마다 이렇게 따로 DTO 를 두는 것도 하나의 방법

        public TransactionListRespDto(Account account, List<Transaction> transactions) {
            this.transactions = transactions.stream().map(
                    transaction -> new TransactionViewDto(transaction, account.getNumber())
            ).collect(Collectors.toList());
        }

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
                        this.balance = transaction.getWithdrawAccountBalance(); // 나한테 출금한 쪽이니, 해당 Tx 에선 출금 잔액에 내 잔액이 남았을 것이기 때문
                    }
                }
            }

        }
    }
}


